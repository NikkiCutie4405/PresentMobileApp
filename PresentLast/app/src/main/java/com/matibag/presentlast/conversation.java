package com.matibag.presentlast;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class conversation extends Activity {
    TextView txtUserName;
    EditText edtMessageInput;
    Button btnSendMessage, btnBack;
    LinearLayout conversationContainer;
    ScrollView messagesScrollView;
    String userName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation);

        // Initialize views
        txtUserName = findViewById(R.id.txtUserName);
        edtMessageInput = findViewById(R.id.edtMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnBack = findViewById(R.id.btnBack);
        conversationContainer = findViewById(R.id.conversationContainer);
        messagesScrollView = findViewById(R.id.messagesScrollView);

        // Get the user name from intent
        userName = getIntent().getStringExtra("userName");
        if (userName != null) {
            txtUserName.setText(userName);
        }

        // Load existing messages
        loadMessages();

        // Send message button click
        btnSendMessage.setOnClickListener(view -> {
            sendMessage();
        });

        // Back button click
        btnBack.setOnClickListener(view -> {
            finish();
        });
    }

    private void loadMessages() {
        // TODO: Load messages from database
        // Example messages for demonstration
        addReceivedMessage("Hey, did you finish the assignment?", "10:30 AM");
        addSentMessage("Yes! Just submitted it.", "10:32 AM");
        addReceivedMessage("Great! Can you help me with question 3? I'm having trouble understanding the concept.", "10:35 AM");
        addSentMessage("Sure! Which part are you stuck on? Let me know and I'll try to explain it better.", "10:37 AM");

        // Scroll to bottom
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = edtMessageInput.getText().toString().trim();

        if (!messageText.isEmpty()) {
            // Get current time
            String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

            // Add the sent message to the UI
            addSentMessage(messageText, currentTime);

            // TODO: Save message to database

            // Clear input field
            edtMessageInput.setText("");

            // Scroll to bottom
            scrollToBottom();
        }
    }

    private void addReceivedMessage(String message, String time) {
        // Inflate message layout
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setGravity(Gravity.START);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, dpToPx(12));
        messageLayout.setLayoutParams(layoutParams);

        // Create message bubble
        LinearLayout bubble = createMessageBubble(message, time, false);

        messageLayout.addView(bubble);
        conversationContainer.addView(messageLayout);
    }

    private void addSentMessage(String message, String time) {
        // Inflate message layout
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setGravity(Gravity.END);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, dpToPx(12));
        messageLayout.setLayoutParams(layoutParams);

        // Create message bubble
        LinearLayout bubble = createMessageBubble(message, time, true);

        messageLayout.addView(bubble);
        conversationContainer.addView(messageLayout);
    }

    private LinearLayout createMessageBubble(String message, String time, boolean isSent) {
        LinearLayout bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackgroundColor(isSent ? 0xFF2563EB : 0xFF2D2D2D);
        int padding = dpToPx(12);
        bubble.setPadding(padding, padding, padding, padding);

        // Set layout params for the bubble - max 70% of screen width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int maxBubbleWidth = (int) (screenWidth * 0.7);

        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // Add margins to keep bubbles away from edges
        if (isSent) {
            bubbleParams.setMargins(dpToPx(60), 0, dpToPx(8), 0);
        } else {
            bubbleParams.setMargins(dpToPx(8), 0, dpToPx(60), 0);
        }
        bubble.setLayoutParams(bubbleParams);

        // Message text with proper wrapping
        TextView txtMessage = new TextView(this);
        txtMessage.setText(message);
        txtMessage.setTextColor(0xFFFFFFFF);
        txtMessage.setTextSize(14);
        txtMessage.setMaxWidth(maxBubbleWidth - dpToPx(24)); // Subtract padding
        txtMessage.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        bubble.addView(txtMessage);

        // Time text
        TextView txtTime = new TextView(this);
        txtTime.setText(time);
        txtTime.setTextColor(isSent ? 0xFFE0E7FF : 0xFF9CA3AF);
        txtTime.setTextSize(10);
        txtTime.setGravity(isSent ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        timeParams.setMargins(0, dpToPx(4), 0, 0);
        txtTime.setLayoutParams(timeParams);
        bubble.addView(txtTime);

        return bubble;
    }

    private void scrollToBottom() {
        messagesScrollView.post(() -> messagesScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}