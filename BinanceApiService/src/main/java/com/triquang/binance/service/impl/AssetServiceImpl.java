package com.triquang.binance.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.triquang.binance.model.Asset;
import com.triquang.binance.model.Coin;
import com.triquang.binance.model.User;
import com.triquang.binance.repository.AssetRepository;
import com.triquang.binance.service.AssetService;

@Service
public class AssetServiceImpl implements AssetService {
	@Autowired
	private AssetRepository assetRepository;

	@Override
	public Asset createAsset(User user, Coin coin, double quantity) {
		Asset asset = new Asset();
		asset.setUser(user);
		asset.setCoin(coin);
		asset.setQuantity(quantity);
		asset.setBuyPrice(coin.getCurrentPrice());
		return assetRepository.save(asset);
	}

	@Override
	public Asset getAssetById(Long assetId) throws Exception {
		return assetRepository.findById(assetId).orElseThrow(() -> new Exception("Asset not found"));
	}

	@Override
	public Asset getAssetByUserIdAndId(Long userId, Long assetId) {
		return null;
	}

	@Override
	public List<Asset> getUsersAsset(Long userId) {
		return assetRepository.findByUserId(userId);
	}

	@Override
	public Asset updateAsset(Long assetId, double quantity) throws Exception {
		Asset oldAsset = getAssetById(assetId);
		oldAsset.setQuantity(quantity + oldAsset.getQuantity());
		return assetRepository.save(oldAsset);
	}

	@Override
	public Asset findAssetByUserIdAndCoinId(Long userId, String coinId) {
		return assetRepository.findByUserIdAndCoinId(userId, coinId);
	}

	@Override
	public void deleteAsset(Long assetId) {
		assetRepository.deleteById(assetId);

	}

}
