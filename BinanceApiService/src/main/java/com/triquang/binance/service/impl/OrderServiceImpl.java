package com.triquang.binance.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.triquang.binance.domain.OrderStatus;
import com.triquang.binance.domain.OrderType;
import com.triquang.binance.model.Asset;
import com.triquang.binance.model.Coin;
import com.triquang.binance.model.Order;
import com.triquang.binance.model.OrderItem;
import com.triquang.binance.model.User;
import com.triquang.binance.repository.OrderItemRepository;
import com.triquang.binance.repository.OrderRepository;
import com.triquang.binance.service.AssetService;
import com.triquang.binance.service.OrderService;
import com.triquang.binance.service.WalletService;

@Service
public class OrderServiceImpl implements OrderService {
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private WalletService walletService;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private AssetService assetService;

	@Override
	public Order createOrder(User user, OrderItem item, OrderType type) {
		double price = item.getCoin().getCurrentPrice() * item.getQuantity();

		Order order = new Order();
		order.setUser(user);
		order.setOrderItem(item);
		order.setOrderType(type);
		order.setPrice(BigDecimal.valueOf(price));
		order.setTimestamp(LocalDateTime.now());
		order.setStatus(OrderStatus.PENDING);

		return orderRepository.save(order);
	}

	@Override
	public Order getOrderById(Long orderId) throws Exception {
		return orderRepository.findById(orderId).orElseThrow(() -> new Exception("Order not found"));
	}

	@Override
	public List<Order> getAllOrdersOfUser(Long userId, OrderType orderType, String assetSymbo) {
		return orderRepository.findByUserId(userId);
	}

	@Override
	@Transactional
	public Order processOrder(Coin coin, double quantity, OrderType orderType, User user) throws Exception {
		if (orderType.equals(OrderType.BUY)) {
			return buyAsset(coin, quantity, user);
		} else if (orderType.equals(OrderType.SELL)) {
			return sellAsset(coin, quantity, user);
		}
		throw new Exception("Invalid Order Type");
	}

	private OrderItem createOrderItem(Coin coin, double quantity, double buyPrice, double sellPrice) {
		var item = new OrderItem();
		item.setCoin(coin);
		item.setQuantity(quantity);
		item.setBuyPrice(buyPrice);
		item.setSellPrice(sellPrice);
		return orderItemRepository.save(item);
	}

	@Transactional
	public Order buyAsset(Coin coin, double quantity, User user) throws Exception {
		if (quantity <= 0) {
			throw new Exception("Quantity should be > 0");
		}
		double buyPrice = coin.getCurrentPrice();
		OrderItem orderItem = createOrderItem(coin, quantity, buyPrice, 0);

		Order order = createOrder(user, orderItem, OrderType.BUY);
		orderItem.setOrder(order);

		walletService.payOrderPayment(order, user);
		order.setStatus(OrderStatus.SUCCESS);
		order.setOrderType(OrderType.BUY);

		Asset oldAsset = assetService.findAssetByUserIdAndCoinId(order.getUser().getId(),
				order.getOrderItem().getCoin().getId());
		if (oldAsset == null) {
			assetService.createAsset(user, orderItem.getCoin(), orderItem.getQuantity());
		} else {
			assetService.updateAsset(oldAsset.getId(), quantity);
		}

		return orderRepository.save(order);
	}

	@Transactional
	public Order sellAsset(Coin coin, double quantity, User user) throws Exception {
		if (quantity <= 0) {
			throw new Exception("Quantity should be > 0");
		}
		double sellPrice = coin.getCurrentPrice();

		Asset assetToSell = assetService.findAssetByUserIdAndCoinId(user.getId(), coin.getId());
		double buyPrice = assetToSell.getBuyPrice();
		if (assetToSell != null) {
			OrderItem orderItem = createOrderItem(coin, quantity, buyPrice, sellPrice);
			Order order = createOrder(user, orderItem, OrderType.SELL);
			orderItem.setOrder(order);

			if (assetToSell.getQuantity() >= quantity) {
				order.setStatus(OrderStatus.SUCCESS);
				order.setOrderType(OrderType.SELL);
				Order savedOrder = orderRepository.save(order);

				walletService.payOrderPayment(order, user);

				Asset updatedAsset = assetService.updateAsset(assetToSell.getId(), -quantity);
				if (updatedAsset.getQuantity() * coin.getCurrentPrice() <= 1) {
					assetService.deleteAsset(updatedAsset.getId());
				}
				return savedOrder;
			}

			throw new Exception("Insufficient quantity to sell");
		}
		throw new Exception("Asset not found");
	}

}
