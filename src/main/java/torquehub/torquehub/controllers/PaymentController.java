package torquehub.torquehub.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import torquehub.torquehub.business.interfaces.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> data) {
        try {
            Long amount = Long.parseLong(data.get("amount").toString());  // Amount in cents
            String currency = data.get("currency").toString();
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(amount, currency);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("clientSecret", paymentIntent.getClientSecret());  // Send client secret to frontend

            return ResponseEntity.ok(responseData);

        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
