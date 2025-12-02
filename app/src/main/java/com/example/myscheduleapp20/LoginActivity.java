package com.example.myscheduleapp20;                  // אותה חבילה כמו MainActivity

import androidx.appcompat.app.AppCompatActivity;      // מחלקת בסיס למסכי אפליקציה
import android.os.Bundle;                             // אובייקט שמעביר מצב ל-onCreate
import android.widget.Button;                         // כפתור
import android.widget.EditText;                       // שדה טקסט
import android.widget.Toast;                          // הודעה קופצת קצרה למסך

public class LoginActivity extends AppCompatActivity { // הגדרת מסך ההתחברות

    private EditText etEmail;                         // שדה האימייל
    private EditText etPassword;                      // שדה הסיסמה
    private Button btnLoginConfirm;                   // כפתור "התחבר"
    private Button btnBack;                           // כפתור "חזרה"

    @Override
    protected void onCreate(Bundle savedInstanceState) { // נקודת הכניסה למסך
        super.onCreate(savedInstanceState);           // הפעלה של onCreate הבסיסי
        setContentView(R.layout.activity_login);      // מחבר למסך activity_login.xml

        // קישור הרכיבים מה-XML למשתנים בקוד:
        etEmail = findViewById(R.id.etEmail);         // מוצא את שדה האימייל לפי ה-id
        etPassword = findViewById(R.id.etPassword);   // מוצא את שדה הסיסמה
        btnLoginConfirm = findViewById(R.id.btnLoginConfirm); // מוצא את כפתור "התחבר"
        btnBack = findViewById(R.id.btnBack);         // מוצא את כפתור "חזרה"

        // מה קורה כשמשתמש לוחץ על "התחבר":
        btnLoginConfirm.setOnClickListener(v -> {
            String email = etEmail.getText().toString();     // לוקחים את האימייל שהוזן
            String password = etPassword.getText().toString(); // לוקחים את הסיסמה

            // כרגע רק מציגים הודעה. אחרי שנבדוק שהכול עובד נוסיף ולידציה והתחברות אמיתית.
            Toast.makeText(
                    LoginActivity.this,              // הקונטקסט (המסך הנוכחי)
                    "ניסית להתחבר עם " + email,     // טקסט שיופיע בהודעה
                    Toast.LENGTH_SHORT               // אורך ההצגה
            ).show();
        });

        // מה קורה כשמשתמש לוחץ על "חזרה":
        btnBack.setOnClickListener(v -> {
            finish();                                // סוגר את LoginActivity וחוזר למסך הקודם
        });
    }
}
