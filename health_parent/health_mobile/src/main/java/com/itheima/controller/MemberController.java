package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.itheima.constant.MessageConstant;
import com.itheima.constant.RedisMessageConstant;
import com.itheima.entity.Result;
import com.itheima.pojo.Member;
import com.itheima.service.MemberService;
import com.itheima.utils.DESUtils;
import com.itheima.utils.DateUtils;
import com.itheima.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * 会员登录
 */
@RestController
@RequestMapping("/member")
public class MemberController {

    @Reference
    private MemberService memberService;

    @Autowired
    private JedisPool jedisPool;

    //使用手机号和验证码登录
    @RequestMapping("/login")
    public Result login(HttpServletResponse response, @RequestBody Map map) {

        String telephone = (String) map.get("telephone");
        String validateCode = (String) map.get("validateCode");
        //从Redis中获取缓存的验证码
        String codeInRedis = jedisPool.getResource().get(telephone + RedisMessageConstant.SENDTYPE_LOGIN);

        if (codeInRedis == null || !codeInRedis.equals(validateCode)) {
            //验证码输入错误
            return new Result(false, MessageConstant.VALIDATECODE_ERROR);
        } else {

            //验证码输入正确
            //判断当前用户是否为会员
            Member member = memberService.findByTelephone(telephone);
            if (member == null) {
                //当前用户不是会员，自动完成注册
                member = new Member();
                member.setPhoneNumber(telephone);
                member.setRegTime(new Date());
                memberService.add(member);
            }

            //登录成功
            //写入Cookie，跟踪用户
            // 对Cookie加密
            try {
                String telDes = DESUtils.encrypt(telephone);
                Cookie cookie = new Cookie("login_member_telephone", telDes);
                cookie.setPath("/");//路径
                cookie.setMaxAge(60 * 60 * 24 * 30);//有效期30天
                response.addCookie(cookie);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //保存会员信息到Redis中
            String json = JSON.toJSON(member).toString();
            jedisPool.getResource().setex(telephone, 60 * 30, json);
            return new Result(true, MessageConstant.LOGIN_SUCCESS);
        }
    }

    /**
     * 根据cookie查询用户基本信息
     *
     * @param cookiedata
     * @return
     */
    @RequestMapping("/findMemberByCookie")
    public Result findMemberByCookie(String cookiedata) {
        try {
            // 解密cookie值,先进行特殊字符的处理
            cookiedata =cookiedata.replace("\"","");
            cookiedata= cookiedata.replaceAll(" ","+");
            String phoneNumByCookie = DESUtils.decrypt(cookiedata);
            if (phoneNumByCookie != null && phoneNumByCookie.length() > 0) {
                // 使用cookie值与redis校验,验证用户是否登录,读取redis中存取的会员信息
                String memberStr = jedisPool.getResource().get(phoneNumByCookie);
                if (!(memberStr != null && memberStr.length() > 0)) {
                    return new Result(false,"请先登录");
                }else {
                    // 将redis中获取的信息返回前端
                    Member member = JSONObject.parseObject(memberStr, Member.class);
                    return new Result(true,"获取会员信息成功!",member);
                }
            }else {
                return new Result(false,"请先登录");
            }
        } catch (Exception e) {
            return new Result(false,"获取会员信息失败!");
        }
    }

    // 修改会员基本信息
    @RequestMapping("/updateMemberMsg")
    public Result updateMemberMsg(@RequestBody Member member) {
        try{
            // 判空
            if(member == null){
                return new Result(false,"修改失败,未正确提交会员信息!");
            }
            // 调用服务层的修改功能
            memberService.updateMemberMsg(member);
            // 修改成功后,更新redis缓存信息
            String json = JSON.toJSON(member).toString();
            jedisPool.getResource().setex(member.getPhoneNumber(), 60 * 30, json);
            return new Result(true, "修改会员信息成功!");
        }catch (Exception e){
            return new Result(false,"修改会员信息失败!");
        }
    }


    // 密码重置
    @RequestMapping("/updatePassword")
    public Result updatePassword(String phoneNumber,@RequestBody Map map) {
        try{
            // 判空
            if(map == null || (String)map.get("pass") == null){
                return new Result(false,"密码重置失败,重置的密码为空!");
            }else {
                String password = (String) map.get("pass");
                // 密码加密
                String md5Pass = MD5Utils.md5(password);
                // 调用服务层的修改功能
                memberService.updatePassword(md5Pass,phoneNumber);
                // 修改成功后,清楚对应redis缓存信息,需要重新登录
                try {
                    jedisPool.getResource().del(phoneNumber);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return new Result(true, "修改密码成功!");
            }
        }catch (Exception e){
            return new Result(false,"修改密码失败!");
        }
    }

    // 变更手机号码
    @RequestMapping("/updateTel")
    public Result updateTel(String oldPhone,@RequestBody Map map) {
        try{
            // 判空
            if(map == null || (String)map.get("telephone" ) == null || oldPhone == null){
                return new Result(false,"变更手机号码失败!");
            }else {
                String newPhone = (String) map.get("telephone");
                // 调用服务层的修改功能
                memberService.updateTel(oldPhone,newPhone);
                // 修改成功后,清除对应redis缓存信息,需要重新登录
                try {
                    jedisPool.getResource().del(oldPhone);
                }catch (Exception e){
                    e.printStackTrace();
                }
                return new Result(true, "变更手机成功!");
            }
        }catch (Exception e){
            return new Result(false,"变更手机失败!");
        }
    }
}