package com.yukuai.score;
import org.junit.Test;
import static org.junit.Assert.*;
public class MatchTest {
 @Test public void badmintonRulesAndUndo(){Match m=new Match();m.red=20;m.blue=19;assertEquals("",m.winner());m.change(true,1);assertEquals("red",m.winner());m.change(false,1);assertEquals(19,m.blue);m.undoLast();assertEquals("",m.winner());m.red=29;m.blue=29;m.change(false,1);assertEquals("blue",m.winner());m.change(false,-1);assertEquals("",m.winner());}
 @Test public void tableTennisDeuceAndZero(){Match m=new Match();m.sport="tabletennis";m.change(false,-1);assertEquals(0,m.blue);m.red=10;m.blue=10;m.change(true,1);assertEquals("",m.winner());m.change(true,1);assertEquals("red",m.winner());m.change(true,-1);assertEquals("",m.winner());m.undoLast();assertEquals("red",m.winner());}
 @Test public void archiveRestoreAndSwitch(){Match m=new Match();m.red=21;m.blue=19;m.reset("tabletennis");assertEquals(1,m.history.length());assertEquals(0,m.red);assertEquals("tabletennis",m.sport);m.change(false,1);Match loaded=Match.restore(m.serialize());assertEquals(1,loaded.blue);assertEquals(1,loaded.history.length());loaded.undoLast();assertEquals(0,loaded.blue);loaded.reset("badminton");assertEquals(1,loaded.history.length());}
}
