package database;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import java.util.HashMap;
import java.util.Map;

public class CardKeyRepository {

    private final Firestore db;

    public CardKeyRepository() throws Exception {
        this.db = FirebaseConfig.getDb();
    }
    // SAVE RSA KEY
    public void saveKey(String cardCode, String modulus, String exponent) throws Exception {

        Map<String, Object> data = new HashMap<>();
        data.put("modulus", modulus);
        data.put("exponent", exponent);

        db.collection("card_keys")
          .document(cardCode)
          .set(data)
          .get(); // wait for completion
    }
    // GET RSA KEY
    public String[] getKey(String cardCode) throws Exception {

        DocumentSnapshot doc = db.collection("card_keys")
                                 .document(cardCode)
                                 .get()
                                 .get();

        if (!doc.exists()) return null;

        return new String[]{
                doc.getString("modulus"),
                doc.getString("exponent")
        };
    }

    public void deleteKey(String cardCode) throws Exception {
        db.collection("card_keys")
          .document(cardCode)
          .delete()
          .get();
    }
}
