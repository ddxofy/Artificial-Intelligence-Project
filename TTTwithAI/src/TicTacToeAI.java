/*
      title: TicTacToe AI-ENGINE 
     author: Kris Cieslak
       date: 03.09.2012
    license: http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_US
   language: java
   
   description: 
      Complete game-tree search/decision algorithm.
      NegaMax function without recursion depth checking. 

 
 Methods:
    private void put(int pos)
    private void clear(int pos)
    private boolean Check(int P)
    private int GameOver()
    private int NegaMax(int p)
    private int PosToBit(int pos)
    private int BitToPos(int bitNum)
    
    public boolean Move(int pos, int player)
    public int GenerateMove(int Player)
    public int getX()
    public int getO()
    public int getBoard()
    public void NewGame()
    public int isGameOver() 
 */

public class TicTacToeAI {

    /*
   BOARD (int) :
   00001|00002|00004|00008
   -----+-----+-----+-----
   00016|00032|00064|00128
   -----+-----+-----+-----
   00256|00512|01024|02048
   -----+-----+-----+-----
   04096|08192|16348|32768
   
   X - only player X (1) positions
   O - only player O (-1) positions
   
   X | O = Board
  
     */
    private long X = 0, O = 0;

    /*  -==-=-=-=- put method -==-=-=-=-=-=-=-=-=-=-=-
  
    Turns on (set 1) specified bit.
    
    a pos sign determinates the choice of an variable (player).
  
    "pos" must be in +/-[1,2,4,8,16,32,64,128,256]  
    "pos" > 0 - X move
    "pos" < 0 - O move
    
     */
    private void put(long pos) {
        X = X | pos & -((pos >> 31) + 1) & ~O;
        O = O | -pos & (pos >> 31) & ~X;
    }

    /* -==-=-=-=- clear method -==-=-=-=-=-=-=-=-=-=-=-
    
    Turns off (set 0) specified bit.
    sign doesn't matter.   

     */
    private void clear(long pos) {
        X = X & ~pos;
        O = O & ~pos;
    }

    /* -==-=-=-=- check method -==-=-=-=-=-=-=-=-=-=-=-	

   P - X or Y (not 1/-1) (look at the GameOver method)
 
   Winning bits:
         Row 1 - 0000000000000111 = 0x0007
         Row 1 - 0000000000001110 = 0x000E
         
         Row 2 - 0000000001110000 = 0x0070
         Row 2 - 0000000011100000 = 0x00E0
        
         Row 3 - 0000011100000000 = 0x0700
         Row 3 - 0000111000000000 = 0x0E00
        
         Row 4 - 0111000000000000 = 0x7000
         Row 4 - 1110000000000000 = 0xE000
        
         Col 1 - 0001000100010000 = 0x1110
         Col 1 - 0000 0001 0001 0001 = 0x0111
        
         Col 2 - 0010 0010 0010 0000 = 0x2220
         Col 2 - 0000 0010 0010 0010 = 0x0222
        
         Col 3 - 0100 0100 0100 0000 = 0x4440
         Col 3 - 0000 0100 0100 0100 = 0x0444
         
         Col 4 - 1000 1000 1000 0000 = 0x8880
         Col 4 - 0000 1000 1000 1000 = 0x0888
         
         Diagonal 1 - 1000 0100 0010 0000 =0x8420
         Diagonal 2 - 0000 0100 0010 0001 =0x0421
         Diagonal 3 - 0000 1000 0100 0010 =0x0842
         Diagonal 3 - 0100 0010 0001 0000 =0x4210
         Diagonal 4 - 0001 0010 0100 0000 =0x1240
         Diagonal 4 - 0000 0010 0100 1000 =0x0248
         Diagonal 5 - 0010 0100 1000 1000 =0x2480
         Diagonal 5 - 0000 0001 0010 0100 =0x0124
	
     */
    private boolean Check(long P) {
        return (P & 0x0007) == 0x0007
                || (P & 0x000E) == 0x000E
                || (P & 0x0070) == 0x0070
                || (P & 0x00E0) == 0x00E0
                || (P & 0x0700) == 0x0700
                || (P & 0x0E00) == 0x0E00
                || (P & 0x7000) == 0x7000
                || (P & 0xE000) == 0xE000
                || (P & 0x1110) == 0x1110
                || (P & 0x0111) == 0x0111
                || (P & 0x2220) == 0x2220
                || (P & 0x0222) == 0x0222
                || (P & 0x4440) == 0x4440
                || (P & 0x0444) == 0x0444
                || (P & 0x8880) == 0x8880
                || (P & 0x0888) == 0x0888
                || (P & 0x8420) == 0x8420
                || (P & 0x0421) == 0x0421
                || (P & 0x0842) == 0x0842
                || (P & 0x4210) == 0x4210
                || (P & 0x1240) == 0x1240
                || (P & 0x0248) == 0x0248
                || (P & 0x2480) == 0x2480
                || (P & 0x0124) == 0x0124;
    }

    /* -==-=-=-=- check method -==-=-=-=-=-=-=-=-=-=-=-
    returns
        2048 - X won
        512  - O won
        1024 - drawn
           0 - game still goes on
	
     */
    private int GameOver() {
        return Check(X) ? 262144 : Check(O) ? 65536 : ((X | O) & 65535) == 65535 ? 131072 : 0;
    }

    /* -==-=-=-=- MiniMax method -==-=-=-=-=-=-=-=-=-=-=-
    
    best_value (binary)

          BEST SCORE                    BEST MOVE 
          524288 262144  65536 | 256 128 64  32  16   8   4   2   1
            0    0    0  |  0   0   0   0   0   0   0   0   0
            |    |    |     
   Winner   X    0    O
   
       
    best_value & 0xffff0000 - clear move bit (get score bits)
    
    Worst "best_value" for X - 512 (O wins)
    Worst "best_value" for O - 2048 (X wins)

     */
    private long MiniMax(long p, int depth) {
        //System.out.println("I am in Minimax : " + p);
        if (depth >=3) {
            return p;
        }
        long End = GameOver();
        if (End != 0) {
            return End;
        }

        long best_value = (p == 1) ? 65536 : 262144;
        for (long b = 1; b <= 32768; b = b << 1) {
            long move = (~getBoard() & b);
            if (move != 0) {
                put(p * move);
                long s = MiniMax(-p, depth + 1);
                best_value = p * (s & 0xffff0000) > p
                        * (best_value & 0xffff0000) ? ((s & 0xffff0000) | move)
                                : best_value;
                clear(move);
            }

        }
        return best_value;
    }

    /* -==-=-=-=- PosToBit/BitToPos method -==-=-=-=-=-=-=-=-=-=-=-
   
    Standard field numeration (1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16)  
    bit position (1,2,4,8,16,32,64,128,256)
	
     */
    private long PosToBit(long pos) {
        return (pos >= 1 && pos <= 16) ? ((long) 1 << (long) (pos - 1)) : 0;
    }

    private int BitToPos(long bitNum) {
        int result = 1;
        while ((bitNum = (bitNum >> (long) 1)) > 0) {
            result++;
        }
        return result;
    }

    /*
  -=-= PUBLIC =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=--==-=-=-=-=-=-
     */

 /* -==-=-=-=- Move method -==-=-=-=-=-=-=-=-=-=-=-
    
    player -  1 or -1
    pos in [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]

     */
    public boolean Move(long pos, int player) {
        long p = PosToBit(pos);
        if (p != 0 && (player == 1 || player == -1) && ((X | O) & p) == 0) {
            put(p * player);
            return true;
        } else {
            return false;
        }
    }

    /* -==-=-=-=- GenerateMoveNegaMax method -==-=-=-=-=-=-=-=-=-=-=-
      Opponent = 1, -1
     */
    public int GenerateMove(int Player) {

        long got = (MiniMax(Player, 0) & 65535);
        long bb = getBoard();
        System.out.println("Here I got : " + Long.toBinaryString(got) + " but " + Long.toBinaryString(getBoard()));
        return BitToPos(got);
    }

// -==-=-=-=- getX -==-=-=-=-=-=-=-=-=-=-=- 	
    public long getX() {
        return X;
    }

// -==-=-=-=- getO -==-=-=-=-=-=-=-=-=-=-=-	
    public long getO() {
        return O;
    }

// -==-=-=-=- getBoard -==-=-=-=-=-=-=-=-=-=-=-
    public long getBoard() {
        return (X | O);
    }

// -==-=-=-=- ClearBoard -==-=-=-=-=-=-=-=-=-=-=-	
    public void NewGame() {
        X = O = 0;
    }

    /* 1 - X player won
	  -1 - O player won
	   2 - drawn
	   0 - game still goes on
     */
// -==-=-=-=-= isGameOver =-=-=-=-=-=-=-=-=-=-=-=-=-
    public int isGameOver() {
        return Check(X) ? 1 : Check(O) ? -1 : ((X | O) & 65535) == 65535 ? 2 : 0;
    }
}