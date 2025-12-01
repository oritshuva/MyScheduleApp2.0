package com.example.myscheduleapp20;
import androidx.appcompat.app.AppCompatActivity;    // מחלקת בסיס לכל Activity בסגנון AppCompat
import android.content.Intent;                      // מחלקה שמאפשרת מעבר בין Activities
import android.os.Bundle;                           // מעביר מידע ל-onCreate (מצב קודם וכו')
import android.widget.Button;                       // מחלקה לכפתורים

public class MainActivity extends AppCompatActivity {   // ההגדרה של מסך הראשי (Activity)

    private Button btnLogin;                        // משתנה לכפתור "התחברות"
    private Button btnSignUp;                       // משתנה לכפתור "הרשמה"

    @Override                                       // מציין שאנחנו מדרסים את onCreate מה-Activity
    protected void onCreate(Bundle savedInstanceState) {  // פונקציה שרצה כשהמסך נטען
        super.onCreate(savedInstanceState);         // קריאה ללוגיקה הבסיסית של ה-Activity
        setContentView(R.layout.activity_main);     // מחבר את המסך לקובץ ה-XML שלנו (activity_main.xml)

        // קישור בין הכפתורים ב-XML לבין המשתנים בקוד
        btnLogin = findViewById(R.id.btnLogin);     // מחפש את הכפתור עם id=btnLogin בקובץ ה-XML
        btnSignUp = findViewById(R.id.btnSignUp);   // מחפש את הכפתור עם id=btnSignUp

        // מה קורה כשמשתמש לוחץ על "התחברות"
//        btnLogin.setOnClickListener(v -> {          // מאזין ללחיצה על כפתור ההתחברות
//            Intent intent = new Intent(             // יוצר Intent – בקשה לפתוח מסך חדש
//                    MainActivity.this,              // מאיזה מסך יוצאים (המסך הנוכחי)
//                    LoginActivity.class             // לאיזה מסך נכנסים (LoginActivity)
//            );
//            startActivity(intent);                  // מפעיל את המסך החדש
//        });

        // מה קורה כשמשתמש לוחץ על "הרשמה"
//        btnSignUp.setOnClickListener(v -> {         // מאזין ללחיצה על כפתור ההרשמה
//            Intent intent = new Intent(             // Intent חדש למסך ההרשמה
//                    MainActivity.this,              // המסך הנוכחי
//                    SignUpActivity.class            // ה-Activity של מסך ההרשמה
//            );
//            startActivity(intent);                  // מפעיל את SignUpActivity
//        });
    }
}
