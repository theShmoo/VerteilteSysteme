package message.response;

import message.Response;
import model.UserInfo;

import java.util.List;

/**
 * Retrieves information about each user, containing username, login status (online/offline) and credits.
 * <p/>
 * E.g.:
 * <pre>
 * &gt; !users
 * 1. alice online Credits: 200
 * 2. bill offline Credits: 180
 * </pre>
 */
public class UserInfoResponse implements Response {
	private static final long serialVersionUID = -6473945861348730298L;

	private List<UserInfo> userInfo;

	public UserInfoResponse(List<UserInfo> userInfo) {
		this.userInfo = userInfo;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getUserInfo().size(); i++) {
			sb.append(i + 1).append(". ").append(getUserInfo().get(i)).append("\n");
		}
		return sb.length() > 0 ? sb.toString() : "No users known.\n";
	}

	public List<UserInfo> getUserInfo() {
		return userInfo;
	}
}
