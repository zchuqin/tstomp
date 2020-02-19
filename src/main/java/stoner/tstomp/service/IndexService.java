package stoner.tstomp.service;

import org.apache.shiro.authc.AuthenticationToken;
import stoner.tstomp.bean.User;

public interface IndexService {

    String login(AuthenticationToken authenticationToken);

    User getUser(String name);
}
