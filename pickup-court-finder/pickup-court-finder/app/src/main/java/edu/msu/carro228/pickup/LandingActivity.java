package edu.msu.carro228.pickup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import edu.msu.carro228.pickup.Utility.Utility;

/**
 * Initial activity / menu
 */
public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
    }

    /**
     * Button handler for join game, joined games, and my games buttons
     * @param view
     */
    public void onGameButton(View view){
        String text = ((Button) view).getText().toString();
        Intent intent = new Intent(getApplicationContext(), GamesActivity.class);
        int mode = 0;
        if (text == getString(R.string.button_join)){
            mode = Utility.Games.JOIN;
        }else if (text == getString(R.string.button_my_games)){
            mode = Utility.Games.MINE;
        }else if (text == getString(R.string.button_joined_games)){
            mode = Utility.Games.JOINED;
        }
        intent.putExtra(Utility.Games.MODE, mode);
        startActivity(intent);
    }


    /**
     * Button handler for create button
     * @param view
     */
    public void onCreateButton(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
        startActivity(intent);
    }
}
