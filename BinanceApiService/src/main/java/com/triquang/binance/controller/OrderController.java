package com.triquang.binance.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.triquang.binance.domain.OrderType;
import com.triquang.binance.model.Coin;
import com.triquang.binance.model.Order;
import com.triquang.binance.model.User;
import com.triquang.binance.request.CreateOrderRequest;
import com.triquang.binance.service.CoinService;
import com.triquang.binance.service.OrderService;
import com.triquang.binance.service.UserService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@Autowired
	private CoinService coinService;

	@PostMapping("/pay")
	public ResponseEntity<Order> payOrderPayment(@RequestHeader("Authorization") String jwt,
			@RequestBody CreateOrderRequest req) throws Exception {
		User user = userService.findUserProfileByJwt(jwt);
		Coin coin = coinService.findById(req.getCoindId());

		Order order = orderService.processOrder(coin, req.getQuantity(), req.getOrderType(), user);
		return ResponseEntity.ok(order);

	}

	@GetMapping("/{orderId}")
	public ResponseEntity<Order> getOrderById(@RequestHeader("Authorization") String jwt, @PathVariable Long orderId)
			throws Exception {
		User user = userService.findUserProfileByJwt(jwt);

		Order order = orderService.getOrderById(orderId);
		if (order.getUser().getId().equals(user.getId())) {
			return ResponseEntity.ok(order);
		} else {
			throw new Exception("You don't have access");
		}
	}

	@GetMapping()
	public ResponseEntity<List<Order>> getListOrderForUser(@RequestHeader("Authorization") String jwt,
			@RequestParam(required = false) OrderType order_type, 
			@RequestParam(required = false) String asset_symbol)
			throws Exception {

		Long userId = userService.findUserProfileByJwt(jwt).getId();
		List<Order> allOrdersOfUser = orderService.getAllOrdersOfUser(userId, order_type, asset_symbol);
		return ResponseEntity.ok(allOrdersOfUser);

	}

}
