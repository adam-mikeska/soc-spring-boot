package com.projekt.projekt.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {
    @Value("${frontend}")
    private String FRONT_END;
    @Autowired
    private UserPrincipalDetailsService userPrincipalDetailsService;
    @Autowired
    private JwtFilter jwtFilter;

    @Primary
    @Bean
    public FreeMarkerConfigurationFactoryBean factoryBean() {
        FreeMarkerConfigurationFactoryBean bean=new FreeMarkerConfigurationFactoryBean();
        bean.setTemplateLoaderPath("classpath:/templates");
        return bean;
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedPercent(true);
        firewall.setAllowUrlEncodedPeriod(true);
        return firewall;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(FRONT_END).allowCredentials(true);
    }

    public SecurityConfig(UserPrincipalDetailsService userPrincipalDetailsService) {
        this.userPrincipalDetailsService = userPrincipalDetailsService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth)  {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and().authorizeRequests()
                .antMatchers("/api/authenticate").anonymous()
                .antMatchers("/api/register").anonymous()
                .antMatchers("/api/verify-email").anonymous()
                .antMatchers("/api/user").authenticated()
                .antMatchers("/api/update**").authenticated()
                .antMatchers("/api/change**").permitAll()
                .antMatchers(HttpMethod.POST,"/api/image").authenticated() // UPLOAD IMAGE
                .antMatchers(HttpMethod.GET,"/api/image/**").permitAll() // IMAGES IS
                .antMatchers(HttpMethod.GET,"/api/orders").permitAll() // GET USERS'S ORDERS
                .antMatchers(HttpMethod.GET,"/admin/autocomplete/emails").hasAnyAuthority("lock_user","asign_role","send_email","*") // AUTOCOMPLETE
                .antMatchers(HttpMethod.GET,"/admin/autocomplete/products").hasAnyAuthority("update_product","create_product","*") // AUTOCOMPLETE
                .antMatchers(HttpMethod.GET,"/admin/autocomplete/roles").hasAnyAuthority("asign_role","*") // AUTOCOMPLETE
                .antMatchers(HttpMethod.GET,"/admin/autocomplete/brands").hasAnyAuthority("update_product","create_product","discount_product","*") // AUTOCOMPLETE
                .antMatchers(HttpMethod.POST,"/admin/products").hasAnyAuthority("create_product","*") // CREATE PRODUCT
                .antMatchers(HttpMethod.POST,"/admin/products/discount").hasAnyAuthority("discount_product","*") // DISCOUNT PRODUCTS
                .antMatchers(HttpMethod.GET,"/admin/orders").hasAnyAuthority("display_orders","*") // DISPLAY ORDERS
                .antMatchers(HttpMethod.GET,"/admin/roles").hasAnyAuthority("display_roles","*") // DISPLAY ROLES
                .antMatchers(HttpMethod.GET,"/admin/users").hasAnyAuthority("display_users","*") // DISPLAY USERS
                .antMatchers(HttpMethod.GET,"/admin/products").hasAnyAuthority("display_products","*") // DISPLAY PRODUCTS
                .antMatchers(HttpMethod.GET,"/admin/brands").hasAnyAuthority("display_brands","*") // DISPLAY BRANDS
                .antMatchers(HttpMethod.GET,"/admin/coupons").hasAnyAuthority("display_coupons","*") // DISPLAY COUPONS
                .antMatchers(HttpMethod.GET,"/admin/users/**/orders").hasAnyAuthority("display_users","*") // DISPLAY USER'S ORDERS
                .antMatchers(HttpMethod.GET,"/admin/orders/**").hasAnyAuthority("display_orders","*") // DISPLAY ORDER
                .antMatchers(HttpMethod.POST,"/admin/send-email").hasAnyAuthority("send_email","*") // SEND EMAIL
                .antMatchers(HttpMethod.POST,"/admin/users/lock").hasAnyAuthority("lock_user","*") // LOCK USERS
                .antMatchers(HttpMethod.PUT,"/admin/users/**").hasAnyAuthority("update_user","*") // UPDATE USER
                .antMatchers(HttpMethod.PUT,"/admin/users/**/image").hasAnyAuthority("update_user","*") // UPDATE USER'S IMAGE
                .antMatchers(HttpMethod.POST,"/admin/roles").hasAnyAuthority("create_role","*") // CREATE ROLE
                .antMatchers(HttpMethod.PUT,"/admin/roles/**").hasAnyAuthority("update_role","*") // UPDATE ROLE
                .antMatchers(HttpMethod.DELETE,"/admin/roles/**").hasAnyAuthority("delete_role","*") // DELETE ROLE
                .antMatchers(HttpMethod.POST,"/admin/users/asign-role").hasAnyAuthority("asign_role","*") // ASIGN ROLE
                .antMatchers(HttpMethod.PUT,"/admin/orders/**").hasAnyAuthority("update_order","*") // UPDATE ORDER
                .antMatchers(HttpMethod.PUT,"/admin/orders/**/coupon").hasAnyAuthority("update_order","*") // UPDATE COUPON
                .antMatchers(HttpMethod.DELETE,"/admin/orders/**/order-items/**").hasAnyAuthority("update_order","*") // DELETE ORDER ITEM
                .antMatchers(HttpMethod.PUT,"/admin/orders/**/order-items/**").hasAnyAuthority("update_order","*") // UPDATE ORDER ITEM
                .antMatchers(HttpMethod.POST,"/admin/orders/**/order-items").hasAnyAuthority("update_order","*") // ADD NEW ORDER ITEM
                .antMatchers(HttpMethod.POST,"/admin/products/**/product-sizes").hasAnyAuthority("update_product","*") // ADD NEW PRODUCT SIZE
                .antMatchers(HttpMethod.PUT,"/admin/**/product-sizes/**").hasAnyAuthority("update_product","*") // UPDATE PRODUCT SIZE
                .antMatchers(HttpMethod.DELETE,"/admin/**/product-sizes/**").hasAnyAuthority("update_product","*") // DELETE PRODUCT SIZE
                .antMatchers(HttpMethod.GET,"/admin/products/**/**").hasAnyAuthority("update_product","*") // GET PRODUCT
                .antMatchers(HttpMethod.PUT,"/admin/products/**").hasAnyAuthority("update_product","*") // UPDATE PRODUCT
                .antMatchers(HttpMethod.DELETE,"/admin/products/**/images/**").hasAnyAuthority("update_product","*") // DELETE PRODUCT IMAGE
                .antMatchers(HttpMethod.POST,"/admin/products/**/images").hasAnyAuthority("update_product","*") // ADD NEW PRODUCT IMAGE
                .antMatchers(HttpMethod.PUT,"/admin/products/**/images/**").hasAnyAuthority("update_product","*") // EDIT PRODUCT IMAGE
                .antMatchers(HttpMethod.PUT,"/admin/products/**/sale").hasAnyAuthority("update_product","*") // CLOSE OR ENABLE SALE OF PRODUCT
                .antMatchers(HttpMethod.PUT,"/admin/brands/**/images").hasAnyAuthority("update_brand","*") // CHANGE IMAGE OF BRAND
                .antMatchers(HttpMethod.GET,"/admin/brands").hasAnyAuthority("display_brands","*") //  GET BRANDS
                .antMatchers(HttpMethod.POST,"/admin/brands**").hasAnyAuthority("add_brand","*") // ADD NEW BRAND
                .antMatchers(HttpMethod.PUT,"/admin/brands/**").hasAnyAuthority("update_brand","*") // UPDATE BRAND
                .antMatchers(HttpMethod.DELETE,"/admin/brands/**").hasAnyAuthority("delete_brand","*") // DELETE BRAND
                .antMatchers(HttpMethod.POST,"/admin/categories").hasAnyAuthority("add_category","*") // ADD NEW CATEGORY
                .antMatchers(HttpMethod.PUT,"/admin/categories/**").hasAnyAuthority("update_category","*") // UPDATE CATEGORY
                .antMatchers(HttpMethod.DELETE,"/admin/categories/**").hasAnyAuthority("delete_category","*") // DELETE CATEGORY
                .antMatchers(HttpMethod.POST,"/admin/coupons").hasAnyAuthority("create_coupon","*") // CREATE COUPON
                .antMatchers(HttpMethod.PUT,"/admin/coupons/**").hasAnyAuthority("update_coupon","*") // UPDATE COUPON
                .antMatchers(HttpMethod.POST,"/admin/carousel").hasAnyAuthority("add_carousel","*") // ADD CAROUSEL IMAGE
                .antMatchers(HttpMethod.PUT,"/admin/carousel/**").hasAnyAuthority("update_carousel","*") // UPDATE CAROUSEL IMAGE
                .antMatchers(HttpMethod.DELETE,"/admin/carousel/**").hasAnyAuthority("delete_carousel","*") // DELETE CAROUSEL IMAGE
                .antMatchers(HttpMethod.POST,"/admin/alert").hasAnyAuthority("add_alert","*") // ADD ALERT
                .antMatchers(HttpMethod.PUT,"/admin/alert/**").hasAnyAuthority("update_alert","*") // UPDATE ALERT
                .antMatchers(HttpMethod.DELETE,"/admin/alert/**").hasAnyAuthority("delete_alert","*") // DELETE ALERT
                .antMatchers(HttpMethod.PUT,"/admin/payments/**").hasAnyAuthority("update_payment","*") // UPDATE PAYMENT PRICE
                .antMatchers(HttpMethod.PUT,"/admin/deliveries/**").hasAnyAuthority("update_delivery","*") //  UPDATE DELIVERY PRICE
                .antMatchers(HttpMethod.GET,"/admin/dashboard").hasAnyAuthority("display_dashboard","*") //  GET DASHBOARD DATA
                .antMatchers("/orders","/orders/**").permitAll() // ORDERS
                .antMatchers(HttpMethod.PUT,"/delivery").permitAll() // SET DELIVERY
                .antMatchers(HttpMethod.PUT,"/payment").permitAll() // SET PAYMENT
                .antMatchers(HttpMethod.PUT,"/coupon").permitAll() // SET COUPON
                .antMatchers("/cart").permitAll() // GET CART
                .antMatchers("/cart-item").permitAll() // UPDATE AND DELETE CART ITEMS
                .antMatchers("/add-to-cart").permitAll() // ADD TO CART
                .antMatchers("/checkout/**").permitAll() // CHECKOUT
                .antMatchers("/payments").permitAll() // LIST PAYMENTS
                .antMatchers("/deliveries").permitAll() // LIST DELIVERIES
                .antMatchers("/products/autocomplete**").permitAll()
                .antMatchers("/products/productOption/**").permitAll()
                .antMatchers("/products/latest").permitAll()
                .antMatchers("/products/discounted").permitAll()
                .antMatchers("/products/popular").permitAll()
                .antMatchers("/products/**").permitAll()
                .antMatchers("/products/categories").permitAll()
                .antMatchers("/products/search").permitAll()
                .antMatchers("/products/carousel").permitAll()
                .antMatchers("/products/alerts").permitAll()
                .antMatchers("/products/image/**").permitAll()
                .antMatchers("/products/carousel/image/*").permitAll()
                .antMatchers("/products/brands/image/*").permitAll()
                .anyRequest().authenticated();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(this.userPrincipalDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

}
