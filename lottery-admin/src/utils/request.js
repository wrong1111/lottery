import axios from "axios";
import { MessageBox, Message } from "element-ui";
import store from "@/store";
import { getToken } from "@/utils/auth";

// create an axios instance
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  // withCredentials: true, // send cookies when cross-domain requests
  timeout: 5000, // request timeout
});

// request interceptor
service.interceptors.request.use(
  (config) => {
    // do something before request is sent
    config.headers["X-Sys"] = 1;
    config.headers["X-User"] = "admin";

    if (store.getters.token) {
      // let each request carry token
      // ['X-Token'] is a custom headers key
      // please modify it according to the actual situation
      config.headers["X-Access-Token"] = getToken();
    }
    return config;
  },
  (error) => {
    // do something with request error
    console.log(error); // for debug
    return Promise.reject(error);
  }
);

// response interceptor
service.interceptors.response.use(
  /**
   * If you want to get http information such as headers or status
   * Please return  response => response
   */

  /**
   * Determine the request status by custom code
   * Here is just an example
   * You can also judge the status by HTTP Status Code
   */
  (response) => {
    let res = response.data;
   // console.log("#################################################");
  //  console.log("请求日志", res);
   // console.log("#################################################");
    // if the custom code is not 20000, it is judged as an error.
    if (res.errorCode) {
      Message({
        message: res.errorMsg || "出现错误，请稍后再试",
        type: "error",
        duration: 5 * 1000,
      });

      if (
        res.errorCode === "E0752" ||
        res.errorCode === "E0753" ||
        res.errorCode === "E0754" ||
        res.errorCode === "E0758"
      ) {
        // to re-login
        MessageBox.confirm(
          "您已退出，您可以取消留在该页面，或重新登录",
          "确定退出",
          {
            confirmButtonText: "重新登录",
            cancelButtonText: "取消",
            type: "warning",
          }
        ).then(() => {
          store.dispatch("user/resetToken").then(() => {
            location.reload();
          });
        });
      }
      return Promise.reject(new Error(res.message || "出现错误，请稍后再试"));
    } else {
      return res;
    }
  },
  (error) => {
    console.log("err" + error); // for debug
    Message({
      message: error.message,
      type: "error",
      duration: 5 * 1000,
    });
    return Promise.reject(error);
  }
);

export default service;
