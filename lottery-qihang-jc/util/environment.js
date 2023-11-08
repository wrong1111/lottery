/* 后端源码请联系;QQ419367301
飞机Telegram账号:qihang9981 */


//生产环境
const prod = {
   BASE_API: '/api',
  //BASE_API: 'https://QQ419367301.com/api',
}

//开发环境
const dev = {
  BASE_API: 'http://127.0.0.1:48080',
}


/*
=================注意根据需要改变环境================
 */
const environment =dev;
export default {
  BASE_API: environment.BASE_API,
}


/* 后端源码请联系;QQ419367301
飞机Telegram账号:qihang9981 */