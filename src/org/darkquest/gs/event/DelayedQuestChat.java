package org.darkquest.gs.event;

import org.darkquest.gs.model.ChatMessage;
import org.darkquest.gs.model.Npc;
import org.darkquest.gs.model.Player;

public abstract class DelayedQuestChat extends DelayedEvent {
  public int curIndex;
  public String[] messages;
  public Npc npc;
  public Player owner;

  public DelayedQuestChat(Npc npc, Player owner, String[] messages) {
    super(null, 2200);
    this.owner = owner;
    this.npc = npc;
    this.messages = messages;
    curIndex = 0;
  }

  public abstract void finished();

  public void run() {
    owner.informOfNpcMessage(new ChatMessage(npc, messages[curIndex], owner));
    curIndex++;
    if(curIndex == messages.length) {
      finished();
      stop();
      return;
    }
  }
}