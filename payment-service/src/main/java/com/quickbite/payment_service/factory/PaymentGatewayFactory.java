package com.quickbite.payment_service.factory;

import com.quickbite.payment_service.gateway.MercadoPagoGateway;
import com.quickbite.payment_service.gateway.PaymentGateway;
import com.quickbite.payment_service.gateway.WebpayGateway;
import com.quickbite.payment_service.gateway.WalletGateway;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayFactory {

    private final WebpayGateway webpayGateway;
    private final MercadoPagoGateway mercadoPagoGateway;
    private final WalletGateway walletGateway;

    public PaymentGatewayFactory(WebpayGateway webpayGateway, 
                                   MercadoPagoGateway mercadoPagoGateway,
                                   WalletGateway walletGateway) {
        this.webpayGateway = webpayGateway;
        this.mercadoPagoGateway = mercadoPagoGateway;
        this.walletGateway = walletGateway;
    }

    public PaymentGateway getGateway(String paymentMethod) {
        switch (paymentMethod.toUpperCase()) {
            case "WEBPAY":
                return webpayGateway;
            case "MERCADO_PAGO":
                return mercadoPagoGateway;
            case "WALLET":
                return walletGateway;
            default:
                throw new IllegalArgumentException("Método de pago no soportado: " + paymentMethod);
        }
    }
}
