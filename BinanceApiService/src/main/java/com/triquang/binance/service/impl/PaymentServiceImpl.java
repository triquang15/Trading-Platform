package com.triquang.binance.service.impl;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.triquang.binance.domain.PaymentMethod;
import com.triquang.binance.domain.PaymentOrderStatus;
import com.triquang.binance.model.PaymentOrder;
import com.triquang.binance.model.User;
import com.triquang.binance.repository.PaymentOrderRepository;
import com.triquang.binance.response.PaymentResponse;
import com.triquang.binance.service.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {
	@Autowired
	private PaymentOrderRepository paymentOrderRepository;

	@Value("${stripe.api.key}")
	private String stripeKey;

	@Value("${razorpay.api.key}")
	private String razorpayKey;

	@Value("${razorpay.api.secret}")
	private String razorpaySecret;

	@Override
	public PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod) {
		PaymentOrder paymentOrder = new PaymentOrder();
		paymentOrder.setUser(user);
		paymentOrder.setAmount(amount);
		paymentOrder.setPaymentMethod(paymentMethod);
		paymentOrder.setStatus(PaymentOrderStatus.PENDING);
		return paymentOrderRepository.save(paymentOrder);
	}

	@Override
	public PaymentOrder getPaymentOrderById(Long id) throws Exception {
		return paymentOrderRepository.findById(id).orElseThrow(() -> new Exception("Payment order not found"));
	}

	@Override
	public boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws RazorpayException {
		if(paymentOrder.getStatus() == null) {
			paymentOrder.setStatus(PaymentOrderStatus.PENDING);
		}
		if (paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)) {
			if (paymentOrder.getPaymentMethod().equals(PaymentMethod.RAZORPAY)) {
				RazorpayClient razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);
				Payment payment = razorpayClient.payments.fetch(paymentId);

				Integer amount = payment.get("amount");
				String status = payment.get("status");

				if (status.equals("captured")) {
					paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
					return true;
				}
				paymentOrder.setStatus(PaymentOrderStatus.FAILED);
				paymentOrderRepository.save(paymentOrder);
				return false;
			}
			paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
			paymentOrderRepository.save(paymentOrder);
			return true;
		}
		return false;
	}

	@Override
	public PaymentResponse createRazorPayment(User user, Long amount, Long orderId) throws RazorpayException {
		Long amountPayment = amount * 100;

		try {
			RazorpayClient razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);

			JSONObject paymentLinkRequest = new JSONObject();
			paymentLinkRequest.put("amount", amount);
			paymentLinkRequest.put("currency", "USD");

			// Create a JSON object with the customer details
			JSONObject customer = new JSONObject();
			customer.put("name", user.getFullName());
			customer.put("email", user.getEmail());
			paymentLinkRequest.put("customer", customer);

			// Create a JSON object with the notification settings
			JSONObject notify = new JSONObject();
			notify.put("email", true);
			paymentLinkRequest.put("notify", notify);

			// Set the reminder settings
			paymentLinkRequest.put("reminder_enable", true);

			// Set the callback URL and method
			paymentLinkRequest.put("callback_url", "http://localhost:3000/wallet?order_id=" + orderId);
			paymentLinkRequest.put("callback_method", "get");

			// Create the payment link using payment link method
			PaymentLink payment = razorpayClient.paymentLink.create(paymentLinkRequest);
			String paymentLinkId = payment.get("id");
			String paymentLinkUrl = payment.get("short_url");

			var paymentResponse = new PaymentResponse();
			paymentResponse.setPayment_url(paymentLinkUrl);
			return paymentResponse;

		} catch (RazorpayException e) {
			throw new RazorpayException(e.getMessage());
		}
	}

	@Override
	public PaymentResponse createStripePayment(User user, Long amount, Long orderId) throws StripeException {
		Stripe.apiKey = stripeKey;
		SessionCreateParams params = SessionCreateParams.builder()
				.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl("http://localhost:3000/wallet?order_id=" + orderId)
				.setCancelUrl("http://localhost:3000/payment/cancel")
				.addLineItem(SessionCreateParams.LineItem.builder()
						.setQuantity(1L)
						.setPriceData(SessionCreateParams.LineItem.PriceData.builder()
								.setCurrency("usd")
								.setUnitAmount(amount*100)
								.setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
										.setName("Top up wallet").build())
								.build())
						.build())
				.build();
		Session session = Session.create(params);
		PaymentResponse res = new PaymentResponse();
		res.setPayment_url(session.getUrl());
				
		return res;
	}

}