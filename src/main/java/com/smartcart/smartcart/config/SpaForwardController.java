package com.smartcart.smartcart.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    // Forward any non-API, non-asset, non-actuator path
    // to index.html so React Router can handle client-side
    // routes like /products, /cart, /login on direct
    // navigation or page refresh.
    @GetMapping(value = {
            "/products",
            "/products/**",
            "/cart",
            "/orders",
            "/orders/**",
            "/login",
            "/register",
            "/recommendations",
            "/profile"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
