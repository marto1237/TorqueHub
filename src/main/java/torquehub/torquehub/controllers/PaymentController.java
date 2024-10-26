package torquehub.torquehub.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import torquehub.torquehub.business.interfaces.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import torquehub.torquehub.domain.request.payment_dtos.PaymentRequest;
import torquehub.torquehub.domain.response.payment_dtos.PaymentResponse;



@RestController
@Validated
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@RequestBody @Valid PaymentRequest paymentRequest) {
        try {
            PaymentIntent paymentIntent = paymentService.createPaymentIntent(
                    paymentRequest.getAmount(),
                    paymentRequest.getCurrency()
            );

            // Create response
            PaymentResponse paymentResponse = new PaymentResponse(paymentIntent.getClientSecret());

            return ResponseEntity.ok(paymentResponse);

        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentResponse("Payment failed: " + e.getMessage()));
        }
    }
}
