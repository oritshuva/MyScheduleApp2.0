package com.example.myscheduleapp20;                       // אותה חבילה כמו MainActivity

import android.os.Bundle; // מאפשר לקבל מצב בעת פתיחת Activity
import android.text.Editable; // טיפוס שמשמש את TextWatcher
import android.text.TextWatcher; // מאזין לשינויים בשדות טקסט
import android.util.Patterns; // תבניות מוכנות (כמו אימייל)
import android.view.View; // שליטה בנראות רכיבים
import android.widget.ProgressBar; // אינדיקציית טעינה
import android.widget.TextView; // טקסט לחיץ (חזרה ללוגין)
import android.widget.Toast; // הודעות קצרות למשתמש

import androidx.appcompat.app.AppCompatActivity; // מחלקת בסיס ל-Activity

import com.google.android.material.button.MaterialButton; // כפתור Material
import com.google.android.material.textfield.TextInputEditText; // שדה טקסט Material
import com.google.android.material.textfield.TextInputLayout; // מעטפת שמאפשרת error
import com.google.firebase.auth.FirebaseAuth; // Firebase Auth לרישום משתמש

public class SignUpActivity extends AppCompatActivity { // Activity של ההרשמה

    private TextInputLayout tilEmail, tilPassword; // מעטפות להצגת הודעות שגיאה
    private TextInputEditText etEmail, etPassword; // השדות עצמם
    private MaterialButton btnSignup; // כפתור הרשמה
    private ProgressBar progress; // טעינה בזמן בקשה
    private TextView tvGoLogin; // קישור חזרה ללוגין

    private FirebaseAuth auth; // אובייקט FirebaseAuth לביצוע הרשמה

    @Override
    protected void onCreate(Bundle savedInstanceState) { // נקודת הכניסה למסך
        super.onCreate(savedInstanceState); // קריאה למחלקת האב
        setContentView(R.layout.activity_sign_up);
        ; // חיבור המסך ל-XML של ההרשמה
        auth = FirebaseAuth.getInstance(); // קבלת מופע FirebaseAuth

        tilEmail = findViewById(R.id.tilEmail); // קישור למעטפת אימייל
        tilPassword = findViewById(R.id.tilPassword); // קישור למעטפת סיסמה
        etEmail = findViewById(R.id.etEmail); // קישור לשדה אימייל
        etPassword = findViewById(R.id.etPassword); // קישור לשדה סיסמה
        btnSignup = findViewById(R.id.btnSignup); // קישור לכפתור הרשמה
        progress = findViewById(R.id.progress); // קישור לטעינה
        tvGoLogin = findViewById(R.id.tvGoLogin); // קישור לטקסט "התחבר"

        TextWatcher watcher = new TextWatcher() { // מאזין לשינויים בטקסט
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {} // לא משתמשים
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {} // לא משתמשים
            @Override public void afterTextChanged(Editable s) { // אחרי שינוי בשדות
                validateFieldsAndToggleButton(); // בדיקת תקינות והדלקת הכפתור
            }
        };

        etEmail.addTextChangedListener(watcher); // מפעיל מאזין על שדה אימייל
        etPassword.addTextChangedListener(watcher); // מפעיל מאזין על שדה סיסמה

        btnSignup.setOnClickListener(v -> doSignup()); // בלחיצה: ננסה להירשם

        tvGoLogin.setOnClickListener(v -> finish()); // חוזר למסך הקודם (Login) וסוגר את ההרשמה
    }

    private void validateFieldsAndToggleButton() { // בדיקת תקינות קלט
        String email = safeText(etEmail); // קריאת אימייל בצורה בטוחה
        String pass = safeText(etPassword); // קריאת סיסמה בצורה בטוחה

        boolean emailOk = Patterns.EMAIL_ADDRESS.matcher(email).matches(); // אימייל תקין?
        boolean passOk = pass.length() >= 6; // סיסמה לפחות 6 תווים?

        if (email.isEmpty()) { // אם האימייל ריק
            tilEmail.setError("חובה למלא אימייל"); // הצגת שגיאה
        } else if (!emailOk) { // אם לא ריק אבל לא תקין
            tilEmail.setError("אימייל לא תקין"); // הצגת שגיאה
        } else {
            tilEmail.setError(null); // ניקוי שגיאה
        }

        if (pass.isEmpty()) { // אם סיסמה ריקה
            tilPassword.setError("חובה למלא סיסמה"); // הצגת שגיאה
        } else if (!passOk) { // אם קצרה מדי
            tilPassword.setError("סיסמה קצרה מדי"); // הצגת שגיאה
        } else {
            tilPassword.setError(null); // ניקוי שגיאה
        }

        btnSignup.setEnabled(emailOk && passOk); // כפתור עובד רק אם הכל תקין
    }

    private void doSignup() { // הרשמה מול Firebase
        validateFieldsAndToggleButton(); // בדיקה שוב לפני שליחה
        if (!btnSignup.isEnabled()) return; // אם לא תקין—לא שולחים

        String email = safeText(etEmail); // אימייל
        String pass = safeText(etPassword); // סיסמה

        setLoading(true); // UI במצב טעינה

        auth.createUserWithEmailAndPassword(email, pass) // יצירת משתמש חדש בפיירבייס
                .addOnCompleteListener(task -> { // callback בסיום
                    setLoading(false); // חזרה למצב רגיל

                    if (task.isSuccessful()) { // אם נרשם בהצלחה
                        Toast.makeText(this, "נרשמת בהצלחה ✅ עכשיו אפשר להתחבר", Toast.LENGTH_SHORT).show(); // הודעה
                        finish(); // חזרה למסך Login
                    } else { // אם נכשל
                        String msg = (task.getException() != null) // אם יש פירוט שגיאה
                                ? task.getException().getMessage() // הטקסט של השגיאה
                                : "שגיאה בהרשמה"; // fallback
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show(); // הצגת השגיאה
                    }
                });
    }

    private void setLoading(boolean loading) { // מצב טעינה
        progress.setVisibility(loading ? View.VISIBLE : View.GONE); // show/hide progress
        btnSignup.setEnabled(!loading && btnSignup.isEnabled()); // בזמן טעינה: מונע לחיצות כפולות
    }

    private String safeText(TextInputEditText et) { // קריאת טקסט בצורה בטוחה
        return et.getText() == null ? "" : et.getText().toString().trim(); // null->"" אחרת trimmed
    }
}
