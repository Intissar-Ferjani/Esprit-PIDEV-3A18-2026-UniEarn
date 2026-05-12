package uniearn.services.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

/**
 * Service pour gérer les paiements via Stripe
 * Permet de créer des sessions de paiement sécurisées pour les contrats
 */
public class StripePaymentService {

    private static final String CURRENCY = "usd";
    private static boolean initialized = false;

    public static class CheckoutSessionInfo {
        private final String sessionId;
        private final String url;

        public CheckoutSessionInfo(String sessionId, String url) {
            this.sessionId = sessionId;
            this.url = url;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getUrl() {
            return url;
        }
    }

    /**
     * Initialise Stripe avec la clé secrète
     * @param secretKey Clé secrète Stripe (depuis .env ou config)
     */
    public static void initialize(String secretKey) {
        if (!initialized && secretKey != null && !secretKey.isEmpty()) {
            Stripe.apiKey = secretKey;
            initialized = true;
            System.out.println("✓ Stripe initialized with API key");
        }
    }

    /**
     * Vérifie si Stripe est initialisé
     */
    public static boolean isInitialized() {
        return initialized && Stripe.apiKey != null && !Stripe.apiKey.isEmpty();
    }

    /**
     * Crée une session de paiement Stripe Checkout
     *
     * @param contractId ID du contrat
     * @param contractTitle Titre du contrat
     * @param freelancerName Nom du freelancer
     * @param amount Montant en dollars (sera converti en centimes)
     * @param successUrl URL de redirection en cas de succès
     * @param cancelUrl URL de redirection en cas d'annulation
     * @return L'URL de la session Stripe Checkout
     * @throws StripeException Si une erreur Stripe se produit
     */
    public String createCheckoutSession(
            int contractId,
            String contractTitle,
            String freelancerName,
            double amount,
            String successUrl,
            String cancelUrl) throws StripeException {

        return createCheckoutSessionInfo(contractId, contractTitle, freelancerName, amount, successUrl, cancelUrl).getUrl();
    }

    public CheckoutSessionInfo createCheckoutSessionInfo(
            int contractId,
            String contractTitle,
            String freelancerName,
            double amount,
            String successUrl,
            String cancelUrl) throws StripeException {

        if (!isInitialized()) {
            throw new IllegalStateException("Stripe is not initialized. Call initialize() first.");
        }

        // Convertir le montant en centimes (Stripe utilise les centimes)
        long amountInCents = (long) (amount * 100);

        // Créer les paramètres de la session
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(CURRENCY)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Séquestre UniEarn: " + contractTitle)
                                                                .setDescription("Fonds sécurisés pour le freelancer " + freelancerName)
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("contract_id", String.valueOf(contractId))
                .putMetadata("platform", "UniEarn")
                .build();

        // Créer la session
        Session session = Session.create(params);

        System.out.println("✓ Stripe checkout session created: " + session.getId());
        System.out.println("  Contract: " + contractTitle + " ($" + amount + ")");
        System.out.println("  URL: " + session.getUrl());

        return new CheckoutSessionInfo(session.getId(), session.getUrl());
    }

    /**
     * Récupère une session Stripe par son ID
     * @param sessionId ID de la session
     * @return La session Stripe
     * @throws StripeException Si une erreur se produit
     */
    public Session retrieveSession(String sessionId) throws StripeException {
        if (!isInitialized()) {
            throw new IllegalStateException("Stripe is not initialized.");
        }
        return Session.retrieve(sessionId);
    }

    /**
     * Vérifie si une session de paiement a été complétée
     * @param sessionId ID de la session
     * @return true si le paiement est complété
     */
    public boolean isSessionCompleted(String sessionId) {
        try {
            Session session = retrieveSession(sessionId);
            return "complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus());
        } catch (StripeException e) {
            System.err.println("Error checking session status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Simule un paiement pour les tests/démos (sans utiliser Stripe)
     * @return true si la simulation réussit
     */
    public boolean simulatePayment() {
        System.out.println("⚠ SIMULATION MODE: Payment simulated successfully");
        return true;
    }

    /**
     * Désactive l'initialisation Stripe (utile pour les tests)
     */
    public static void reset() {
        initialized = false;
        Stripe.apiKey = null;
    }
}

