package torquehub.torquehub.business.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;
import torquehub.torquehub.business.interfaces.PaymentService;

@Service
public class PaymentServiceImpl implements PaymentService {

    public PaymentIntent createPaymentIntent(Long amount, String currency) throws StripeException {
        PaymentIntentCreateParams createParams = new PaymentIntentCreateParams.Builder()
                .setCurrency(currency)// Amount in cents (e.g., 5000 = 50 Euros)
                .setAmount(amount) // Amount in cents
                .build();
        return PaymentIntent.create(createParams);
    }


}
