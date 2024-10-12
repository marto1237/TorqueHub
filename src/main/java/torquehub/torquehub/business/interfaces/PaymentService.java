package torquehub.torquehub.business.interfaces;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

@Service
public interface PaymentService {

    PaymentIntent createPaymentIntent(Long amount, String currency) throws StripeException;
}
