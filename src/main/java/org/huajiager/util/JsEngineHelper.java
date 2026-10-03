package org.huajiager.util;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaScript 引擎加载器（Nashorn 替代方案）。
 *
 * Java 8 时代使用 Nashorn 引擎（`new ScriptEngineManager().getEngineByName("nashorn")`）
 * 驱动替身(stand) JS 脚本与自定义 JS 动画。Fabric 1.20.1 需要 Java 17，而 JDK 15+ 已移除 Nashorn，
 * 因此通过 GraalJS（org.graalvm.js:js + js-scriptengine）提供 javax.script.ScriptEngine 兼容实现。
 *
 * 此处按多引擎名依次 fallback，以便在缺少 GraalJS 时仍可尝试其他可用引擎。
 * <p>初始化要点（均为运行期已验证的坑）：
 * <ul>
 * <li>GraalJS 经 javax.script 桥默认不暴露 {@code Java} 全局（JS 脚本里的 {@code Java.type} 会抛
 *     ReferenceError），需在引擎创建前开启 {@code polyglot.js.nashorn-compat=true}；</li>
 * <li>nashorn-compat 提供 {@code Java.type}，但 GraalJS 并无 Nashorn 特有的
 *     {@code Java.asJSONCompatible}，需注入幂等 polyfill：脚本里用它包裹 JSON 字面量只是为了把
 *     其结果当 {@code java.util.Map} 读，而 GraalJS 的脚本对象本就实现 Map，原样返回即可。</li>
 * </ul></p>
 */
public final class JsEngineHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsEngineHelper.class);

	/**
	 * 每个线程一份引擎：GraalJS 的 Context 有线程归属，跨线程调用会抛
	 * "Multi threaded access requested by thread ... but is not allowed for language(s) js"。
	 * 资源重载线程与渲染线程各自持有一份即可（加锁无法解决，Context 不允许换线程）。
	 */
	private static final ThreadLocal<ScriptEngine> THREAD_ENGINES =
			ThreadLocal.withInitial(JsEngineHelper::createEngine);

	/** 全局共享的 JS 引擎入口：实际按调用线程分派到该线程自己的引擎。 */
	public static final ScriptEngine ENGINE = (ScriptEngine) java.lang.reflect.Proxy.newProxyInstance(
			JsEngineHelper.class.getClassLoader(),
			new Class<?>[] { ScriptEngine.class },
			JsEngineHelper::invokeThreadEngine);

	/** 把 ScriptEngine 的调用转发到当前线程的引擎实例。 */
	private static Object invokeThreadEngine(Object proxy, java.lang.reflect.Method method, Object[] args)
			throws Throwable {
		if (method.getDeclaringClass() == Object.class) {
			return switch (method.getName()) {
				case "toString" -> "ThreadLocalScriptEngine";
				case "hashCode" -> System.identityHashCode(proxy);
				case "equals" -> proxy == args[0];
				default -> null;
			};
		}
		try {
			return method.invoke(THREAD_ENGINES.get(), args);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw e.getCause();
		}
	}

	/** 引擎兼容层初始化脚本：开启行为的 polyfill 统一在此注入。 */
	private static final String COMPAT_BOOT_SCRIPT =
			"if (typeof Java !== 'undefined' && typeof Java.asJSONCompatible === 'undefined') {"
					+ " Java.asJSONCompatible = function (o) { return o; };"
					+ "}";

	private JsEngineHelper() {
	}

	private static ScriptEngine createEngine() {
		// GraalJS 需在 Context 创建前启用 Nashorn 兼容模式，否则 JS 中 Java.type 不可用
		System.setProperty("polyglot.js.nashorn-compat", "true");
		ScriptEngineManager manager = new ScriptEngineManager();
		// "graal.js" 为 GraalJS js-scriptengine 注册的引擎名；其后为通用兜底名
		String[] candidates = { "graal.js", "js", "javascript", "nashorn" };
		for (String name : candidates) {
			try {
				ScriptEngine engine = manager.getEngineByName(name);
				if (engine != null) {
					LOGGER.info("[HuajiAge] JavaScript engine '{}' ({} {}) loaded",
							name,
							engine.getFactory().getEngineName(),
							engine.getFactory().getEngineVersion());
					bootstrap(engine);
					return engine;
				}
			} catch (Exception e) {
				LOGGER.warn("[HuajiAge] Engine '{}' unavailable: {}", name, e.toString());
			}
		}
		throw new IllegalStateException(
				"[HuajiAge] No JavaScript engine found. GraalJS (org.graalvm.js:js-scriptengine) is required for stand scripts.");
	}

	/** 引擎级兼容初始化：注入 {@code Java.asJSONCompatible} polyfill 等。 */
	private static void bootstrap(ScriptEngine engine) {
		try {
			engine.getContext().setAttribute("polyglot.js.nashorn-compat", true, ScriptContext.ENGINE_SCOPE);
			engine.eval(COMPAT_BOOT_SCRIPT);
		} catch (ScriptException | RuntimeException e) {
			// 兜底引擎（如真 Nashorn）不缺 asJSONCompatible，注入失败不应阻断启动
			LOGGER.warn("[HuajiAge] JS engine bootstrap skipped: {}", e.toString());
		}
	}
}
