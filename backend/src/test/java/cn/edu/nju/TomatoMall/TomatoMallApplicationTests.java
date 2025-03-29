package cn.edu.nju.TomatoMall;

import cn.edu.nju.TomatoMall.models.po.User;
import cn.edu.nju.TomatoMall.util.SecurityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TomatoMallApplicationTests {

	@Autowired
	SecurityUtil securityUtil;

	@Test
	void contextLoads() {
		User user=new User();
		user.setId(1);
		user.setPassword("123456");
		System.out.println(securityUtil.getToken(user));
	}

}
