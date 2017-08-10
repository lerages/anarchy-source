package org.rs2server.rs2.domain.model.player;

import lombok.Getter;
import lombok.Setter;
import org.rs2server.rs2.domain.service.api.content.PrivateChatService;
import org.rs2server.rs2.domain.service.api.content.PrivateChatService.PrivateChatStatus;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Clank1337
 */
public final
@Getter @Setter
class PlayerPrivateChatEntity {

	Set<String> friendsList;
	Set<String> ignoreList;
	PrivateChatService.PrivateChatStatus status;
	public Set<String> getFriendsList() {
		// TODO Auto-generated method stub
		return friendsList;
	}
	public void setFriendsList(HashSet hashSet) {
		this.friendsList = hashSet;
		return;
		// TODO Auto-generated method stub
		
	}
	public Set<String> getIgnoreList() {
		// TODO Auto-generated method stub
		return ignoreList;
	}
	public void setIgnoreList(HashSet hashSet) {
		this.ignoreList = hashSet;
		return;
		// TODO Auto-generated method stub
		
	}
	public PrivateChatStatus getStatus() {
		// TODO Auto-generated method stub
		return status;
	}
	public void setStatus(PrivateChatStatus on) {
		this.status = on;
		return;
		// TODO Auto-generated method stub
		
	}

}
