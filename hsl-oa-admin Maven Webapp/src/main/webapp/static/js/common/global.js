/**
 * 验证密码及长度
 * @param {Object} v
 */
function checkPassWord(v){
        var numasc = 0;
        var charasc = 0;
        var otherasc = 0;
        if(0==v.length){
            return "密码不能为空";
        }else if(v.length<8||v.length>12){
            return "密码至少8个字符,最多12个字符";
        }else{
            for (var i = 0; i < v.length; i++) {
                var asciiNumber = v.substr(i, 1).charCodeAt();
                if (asciiNumber >= 48 && asciiNumber <= 57) {
                    numasc += 1;
                }
                if ((asciiNumber >= 65 && asciiNumber <= 90)||(asciiNumber >= 97 && asciiNumber <= 122)) {
                    charasc += 1;
                }
                if ((asciiNumber >= 33 && asciiNumber <= 47)||(asciiNumber >= 58 && asciiNumber <= 64)||(asciiNumber >= 91 && asciiNumber <= 96)||(asciiNumber >= 123 && asciiNumber <= 126)) {
                    otherasc += 1;
                }
            }
            if(0==numasc)  {
                return "密码必须含有数字";
            }else if(0==charasc){
                return "密码必须含有字母";
            }else if(0==otherasc){
                return "密码必须含有特殊字符";
            }else{
                return 8888;
            }
        }
};