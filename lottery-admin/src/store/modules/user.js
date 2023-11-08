import { login, logout, getInfo } from "@/api/user";
import {
  getToken,
  setToken,
  removeToken,
  getUser,
  setUser,
  removeUser,
} from "@/utils/auth";
import { resetRouter } from "@/router";

// 解析本地用户信息
let userInfo = getUser();
let user = null;
if (userInfo) {
  user = JSON.parse(userInfo);
}

console.log(userInfo);

const getDefaultState = () => {
  return {
    token: getToken(),
    name: user && user.name,
    avatar: user && user.avatar,
    username: user && user.username,
    adminShop: user && user.adminShop,
  };
};

const state = getDefaultState();

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState());
  },
  SET_TOKEN: (state, token) => {
    state.token = token;
  },
  SET_NAME: (state, name) => {
    state.name = name;
  },
  SET_USERNAME: (state, username) => {
    state.username = username;
  },
  SET_ADMINSHOP: (state, adminShop) => {
    state.adminShop = adminShop;
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar;
  },
};

const actions = {
  // user login
  async login({ commit }, userInfo) {
    const { username, password } = userInfo;
    let result = await login({ username: username.trim(), password: password });
    if (!result.errorCode) {
      commit("SET_TOKEN", result.token);
      commit("SET_NAME", result.name);
      commit("SET_USERNAME", result.username);
      commit("SET_AVATAR", result.avatar);
      commit("SET_ADMINSHOP", result.adminShop);
      setToken(result.token);
      setUser(result);

      return "成功";
    } else {
      return Promise.reject(new Error(result.errorMsg || "Error"));
    }
  },

  // get user info
  async getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      let user = JSON.parse(getUser());

      if (user && user.name) {
        resolve();
      } else {
        reject("获取用户信息失败");
      }
    });
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      removeToken(); // must remove  token  first
      removeUser();
      resetRouter();
      commit("RESET_STATE");
      resolve();
    });
  },

  // remove token
  resetToken({ commit }) {
    return new Promise((resolve) => {
      removeToken(); // must remove  token  first
      removeUser();
      commit("RESET_STATE");
      resolve();
    });
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
};
