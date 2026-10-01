package org.huajiager.api;

import java.util.ArrayList;
import java.util.List;

/**
 * HuajiAge API
 */
public class HuajiAgeAPIImpl implements HuajiAgeAPI.IHuajiAgeAPI {

	private final List<IMultiBlock> multiBlockList = new ArrayList<>();
	private final List<IStand> standList = new ArrayList<>();
	private final List<IStandState> standStateList = new ArrayList<>();

	@Override
	public void registerMultiBlock(IMultiBlock multiBlock) {
		multiBlockList.add(multiBlock);
	}

	@Override
	public List<IMultiBlock> getMultiBlockList() {
		return multiBlockList;
	}

	@Override
	public void registerStand(IStand stand) {
		standList.add(stand);
	}

	@Override
	public List<IStand> getStandList() {
		return standList;
	}

	@Override
	public void registerStandState(IStandState stand) {
		standStateList.add(stand);
	}

	@Override
	public List<IStandState> getStandStateList() {
		return standStateList;
	}

	@Override
	public void standClear() {
		standList.clear();
	}

	@Override
	public void statesClear() {
		standStateList.clear();
	}
}
