package com.triquang.binance.service;

import com.triquang.binance.model.Coin;
import com.triquang.binance.model.User;
import com.triquang.binance.model.Watchlist;

public interface WatchListService {
	Watchlist findUserWatchList(Long userId) throws Exception;

	Watchlist createWatchList(User user);

	Watchlist findById(Long id) throws Exception;

	Coin addItemToWatchList(Coin coin, User user) throws Exception;
}
