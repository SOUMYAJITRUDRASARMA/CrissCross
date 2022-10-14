package com.example.crisscross;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private class Move
    {
        int player;  // player to transition to next state
        int index;  // index to transition to next state
        int gameStatus;  // gameStatus of current state
        String gameStatusText;

        public Move(int player, int index, int gameStatus, String gameStatusText)
        {
            this.player = player;
            this.index = index;
            this.gameStatus = gameStatus;
            this.gameStatusText = gameStatusText;
        }
    }

    ImageView gameImg[];
    TextView textViewGameStatus;
    int gameStatus, turnNumber;
    int gameArr[];
    final int PLAYER_1 = 1,
              PLAYER_2 = 2,
              GAME_END = 0,
              EMPTY = 0;
    final String STATUS_TURN_PLAYER_1 = "Turn of Player 1",
                 STATUS_TURN_PLAYER_2 = "Turn of Player 2",
                 STATUS_WIN_PLAYER_1 = "Player 1 Wins !!!",
                 STATUS_WIN_PLAYER_2 = "Player 2 Wins !!!",
                 STATUS_DRAW = "Draw !!!";
    Toast alertToast;

    Stack<Move> undoMoves, redoMoves;

    public MainActivity()
    {
        // Game Items ...
        gameStatus = PLAYER_1;
        turnNumber = 0;

        gameArr = new int[9];
        for(int i = 0; i < 9; i++) gameArr[i] = EMPTY;

        undoMoves = new Stack<Move>();
        redoMoves = new Stack<Move>();

        // App Items ...
        gameImg = new ImageView[9];
        alertToast = null;
    }

    private void showToast(String s)
    {
        // to prevent stacking up of toasts timewise ...
        if(alertToast != null) alertToast.cancel();

        alertToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        alertToast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // App Items ...
        textViewGameStatus = (TextView) findViewById(R.id.TextViewStatus);

        gameImg[0] = (ImageView) findViewById(R.id.gameImg1);
        gameImg[1] = (ImageView) findViewById(R.id.gameImg2);
        gameImg[2] = (ImageView) findViewById(R.id.gameImg3);
        gameImg[3] = (ImageView) findViewById(R.id.gameImg4);
        gameImg[4] = (ImageView) findViewById(R.id.gameImg5);
        gameImg[5] = (ImageView) findViewById(R.id.gameImg6);
        gameImg[6] = (ImageView) findViewById(R.id.gameImg7);
        gameImg[7] = (ImageView) findViewById(R.id.gameImg8);
        gameImg[8] = (ImageView) findViewById(R.id.gameImg9);
    }

    boolean checkWin(int index, int player)
    {
        for(int i = 0; i < 3; i++)
        {
            boolean resRow = true, resColumn = true;
            for (int j = 0; j < 3; j++)
            {
                resRow = resRow && gameArr[3 * i + j] == player;
                resColumn = resColumn && gameArr[3 * j + i] == player;
            }

            if(resRow || resColumn) return true;
        }

        boolean resDiagonal1 = true, resDiagonal2 = true;
        for(int i = 0; i < 3; i++)
        {
            resDiagonal1 = resDiagonal1 && gameArr[3 * i + i] == player;
            resDiagonal2 = resDiagonal2 && gameArr[3 * i + 2 - i] == player;
        }

        return resDiagonal1 || resDiagonal2;
    }

    public void gameImgClicked(View view)
    {
        // Log Info ...
        Log.i("UserInfo", "Img Clicked (" + turnNumber + ") !!!");

        // Initial check for game running ...
        if(gameStatus == GAME_END)
        {
            showToast("Game has already ended , restart !!!");
            return;
        }

        int index = -1, player;

        // Getting index ...
        for(int i = 0; i < 9; i++)
            if(view.getId() == gameImg[i].getId()){ index = i; break; }

        if(index == -1)
        {
            showToast("Invalid Button Pressed !!!\n" +
                               "What did you press LOL ... ");
            return;
        }
        else if(gameArr[index] != EMPTY)
        {
            showToast("Cannot set a non-empty cell");
            return;
        }

        // Getting player ...
        player = gameStatus;

        // Setting game states ...
        turnNumber++;
        gameArr[index] = player;
        if(player == PLAYER_1) gameImg[index].setImageResource(R.drawable.cross);
        else gameImg[index].setImageResource(R.drawable.criss);

        // SAVING OUR MOVE INTO STACK AND CLEARING POSSIBLE REDO MOVES ...
        Move curMove = new Move(player, index, gameStatus, (String) textViewGameStatus.getText());
        undoMoves.push(curMove);
        redoMoves.clear();

        // Checking for win ...
        if(checkWin(index, player))
        {
            gameStatus = GAME_END;
            textViewGameStatus.setText((player == PLAYER_1)? STATUS_WIN_PLAYER_1: STATUS_WIN_PLAYER_2);
            return;
        }

        // Checking for draw ...
        if(turnNumber >= 9)
        {
            gameStatus = GAME_END;
            textViewGameStatus.setText(STATUS_DRAW);
            return;
        }

        // Neither win neither draw ...
        gameStatus = (PLAYER_1 + PLAYER_2) - gameStatus;
        textViewGameStatus.setText((gameStatus == PLAYER_1)? STATUS_TURN_PLAYER_1: STATUS_TURN_PLAYER_2);
    }

    public void resetGame(View view)
    {
        // Game Items ...
        turnNumber = 0;
        gameStatus = PLAYER_1;
        for(int i = 0; i < 9; i++) gameArr[i] = EMPTY;
        undoMoves.clear();
        redoMoves.clear();

        // App Items ...
        textViewGameStatus.setText(STATUS_TURN_PLAYER_1);
        for(ImageView img: gameImg) img.setImageResource(R.drawable.none);

        // Alerting User that game has been reset ...
        showToast("Your game has been reset ...");
    }

    public void undoGameMove(View view)
    {
        if(undoMoves.empty())
        {
            showToast("No moves to undo !!!");
            return;
        }

        turnNumber--;
        Move m = undoMoves.pop();

        // Adding to redo stack ...
        Move mRedo = new Move(m.player, m.index, gameStatus, (String) textViewGameStatus.getText());
        redoMoves.push(mRedo);

        // Resetting game constants ...
        gameArr[m.index] = EMPTY;
        gameImg[m.index].setImageResource(R.drawable.none);
        gameStatus = m.gameStatus;
        textViewGameStatus.setText(m.gameStatusText);

        // Letting user know reset is successful ...
        showToast("Move undo successful ...");
    }

    public void redoGameMove(View view)
    {
        if(redoMoves.empty())
        {
            showToast("No moves to redo !!!");
            return;
        }

        turnNumber++;
        Move m = redoMoves.pop();

        // Adding to undo stack ...
        Move mUndo = new Move(m.player, m.index, gameStatus, (String) textViewGameStatus.getText());
        undoMoves.push(mUndo);

        // Resetting game constants ...
        gameArr[m.index] = m.player;
        if(m.player == PLAYER_1) gameImg[m.index].setImageResource(R.drawable.cross);
        else gameImg[m.index].setImageResource(R.drawable.criss);
        gameStatus = m.gameStatus;
        textViewGameStatus.setText(m.gameStatusText);

        // Letting user know redo is successful ...
        showToast("Move redo successful ...");
    }

}