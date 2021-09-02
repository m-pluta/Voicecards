package com.thundercandy.epq;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.thundercandy.epq.data.Card;
import com.thundercandy.epq.data.SessionCard;
import com.thundercandy.epq.database.DbUtils;

import java.util.ArrayList;
import java.util.Collections;

public class SessionActivity extends AppCompatActivity {

    ImageView btnBack;
    SessionManager manager;
    Button btnEndSession, btnKnown, btnUnknown;
    TextView txtTerm, txtDefinition;
    ImageView btnReadTerm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);
        btnBack = findViewById(R.id.btnBack);
        btnEndSession = findViewById(R.id.btnEndSession);
        btnKnown = findViewById(R.id.btnKnown);
        btnUnknown = findViewById(R.id.btnUnknown);
        txtTerm = findViewById(R.id.txtTerm);
        txtDefinition = findViewById(R.id.txtDefinition);
        btnReadTerm = findViewById(R.id.btnReadTerm);

        overridePendingTransition(R.anim.forward_slide_in, R.anim.forward_slide_out);
        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        // This part starts the session with the categories the user selected
        Intent receivedIntent = getIntent();
        ArrayList<Integer> selectedCats = receivedIntent.getIntegerArrayListExtra("selectedCategories");
        ArrayList<SessionCard> scs = extractCardsFromCategories(selectedCats);
        Collections.shuffle(scs);
        manager = new SessionManager(scs);
        manager.start();

        btnEndSession.setOnClickListener(v -> {
            manager.end();
        });
        btnKnown.setOnClickListener(v -> {
            manager.known();
        });
        btnUnknown.setOnClickListener(v -> {
            manager.unknown();
        });
        txtDefinition.setOnClickListener(v -> {
            manager.revealDefinition();
        });


    }

    public ArrayList<SessionCard> extractCardsFromCategories(ArrayList<Integer> selectedCategoryIDs) {
        ArrayList<SessionCard> scs = new ArrayList<>();
        for (Integer i : selectedCategoryIDs) {
            ArrayList<Card> cards = DbUtils.fetchCategoryCards(this, i);

            for (Card c : cards) {
                scs.add(c.toSessionCard());
            }
        }

        for (SessionCard sc : scs) {
            Log.d("CARD_FETCH", sc.toString());
        }
        return scs;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back_slide_in, R.anim.back_slide_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.back_slide_in, R.anim.back_slide_out);
    }

    public class SessionManager {

        ArrayList<SessionCard> cards;
        boolean definitionRevealed = false;
        SessionCard loadedCard;
        boolean ended = false;
        int lastCardID = -1;

        public SessionManager(ArrayList<SessionCard> cards) {
            this.cards = cards;
        }

        public void start() {
            btnEndSession.setEnabled(true);
            btnKnown.setEnabled(true);
            btnUnknown.setEnabled(true);
            loadNextCard();
        }

        public void end() {
            ended = true;
            logData();
        }

        private void logData() {
            String result = "cards={";
            for (SessionCard sc : cards) {
                result += sc.toShortString() + ", ";
            }
            result += "}";

            Log.d("SESSION_END", result);
        }

        public void known() {
            loadedCard.known();
            Collections.rotate(cards, -1);
            loadNextCard();
        }

        public void unknown() {
            loadedCard.unknown();
            Collections.rotate(cards, -1);
            loadNextCard();
        }

        public void revealDefinition() {
            txtDefinition.setText(loadedCard.getDefinition());
            definitionRevealed = true;
            btnKnown.setEnabled(true);
            btnUnknown.setEnabled(true);
        }

        int cardsCycled = 0;
        public void loadNextCard() {
            if (loadedCard != null) {
                lastCardID = loadedCard.getId();
            }
            if (cardsCycled == cards.size()) {
                Collections.shuffle(cards);
                cardsCycled = 0;
            }

            definitionRevealed = false;
            btnKnown.setEnabled(false);     //TODO: replace this with grey buttons or something
            btnUnknown.setEnabled(false);


            if (cards.get(0).getId() == lastCardID) {               // So the user doesnt see the same card two times in a row
                Collections.swap(cards, 0, cards.size() - 1);
            }
            loadedCard = cards.get(0);
            cardsCycled++;

            txtTerm.setText(loadedCard.getTerm());
            txtDefinition.setText("Click to see definition");

        }
    }
}
