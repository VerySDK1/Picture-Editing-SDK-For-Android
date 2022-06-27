package com.pesdk.bean;

import java.util.List;

import androidx.annotation.Keep;

/**
 *
 */
@Keep
public class PageData {

    /**
     * total : 111
     * per_page : 10
     * current_page : 1
     * last_page : 12
     * data : [{"id":"1018207","ufid":"1652845901","name":"404","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/CTVRHZ-EBYPYT-DVBMJM-IEAWIC/templateApiImage/1652845901/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/CTVRHZ-EBYPYT-DVBMJM-IEAWIC/templateApiImage/1652845901/cover.jpg?x-oss-process=style/templateapi","updatetime":0,"height":1080,"width":488,"video":"","appkey":"CTVRHZ-EBYPYT-DVBMJM-IEAWIC","text_need":"0","picture_need":"1","video_need":"0","desc":""},{"id":"1018205","ufid":"1652255662","name":"自在生活","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/39c98bc39ca150af/templateApiImage/1652255662/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/39c98bc39ca150af/templateApiImage/1652255662/cover.png?x-oss-process=style/templateapi","updatetime":0,"height":1080,"width":500,"video":"","appkey":"39c98bc39ca150af","text_need":"0","picture_need":"3","video_need":"0","desc":"i"},{"id":"1018204","ufid":"1652255539","name":"女王节快乐","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/39c98bc39ca150af/templateApiImage/1652255539/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/39c98bc39ca150af/templateApiImage/1652255539/cover.png?x-oss-process=style/templateapi","updatetime":0,"height":1080,"width":608,"video":"","appkey":"39c98bc39ca150af","text_need":"0","picture_need":"3","video_need":"0","desc":"i"},{"id":"1018199","ufid":"1652242326","name":"最美的人儿","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/39c98bc39ca150af/templateApiImage/1652242326/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/39c98bc39ca150af/templateApiImage/1652242326/cover.jpg?x-oss-process=style/templateapi","updatetime":0,"height":1080,"width":500,"video":"","appkey":"39c98bc39ca150af","text_need":"0","picture_need":"1","video_need":"0","desc":"a"},{"id":"1018196","ufid":"1652240217","name":"2222\n测试\n","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652240217/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652240217/cover.png?x-oss-process=style/templateapi","updatetime":0,"height":678,"width":480,"video":"","appkey":"e069b36c568d909f","text_need":"0","picture_need":"1","video_need":"0","desc":"测试"},{"id":"1018193","ufid":"1652176004","name":"你","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652176004/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652176004/cover.png?x-oss-process=style/templateapi","updatetime":0,"height":2048,"width":1536,"video":"","appkey":"e069b36c568d909f","text_need":"0","picture_need":"1","video_need":"0","desc":"你"},{"id":"1018192","ufid":"1652175521","name":"无","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652175521/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652175521/cover.png?x-oss-process=style/templateapi","updatetime":0,"height":2048,"width":1536,"video":"","appkey":"e069b36c568d909f","text_need":"0","picture_need":"0","video_need":"0","desc":"无"},{"id":"1018191","ufid":"1652174853","name":"你","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652174853/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652174853/cover.png?x-oss-process=style/templateapi","updatetime":0,"height":2048,"width":1536,"video":"","appkey":"e069b36c568d909f","text_need":"0","picture_need":"0","video_need":"0","desc":"无"},{"id":"1018190","ufid":"1652173623","name":"我","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652173623/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652173623/cover.png?x-oss-process=style/templateapi","updatetime":0,"height":2048,"width":1536,"video":"","appkey":"e069b36c568d909f","text_need":"0","picture_need":"1","video_need":"0","desc":"他"},{"id":"1018189","ufid":"1652172878","name":"回到好好的\n","file":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652172878/file.zip","cover":"http://rdfile.oss-cn-hangzhou.aliyuncs.com/pesystem/e069b36c568d909f/templateApiImage/1652172878/cover.png?x-oss-process=style/templateapi","updatetime":0,"height":2048,"width":1536,"video":"","appkey":"e069b36c568d909f","text_need":"1","picture_need":"1","video_need":"0","desc":"反季节发\n"}]
     */

    private int total;
    private int per_page;
    private int current_page;
    private int last_page;
    private List<DataBean> data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPer_page() {
        return per_page;
    }

    public void setPer_page(int per_page) {
        this.per_page = per_page;
    }

    public int getCurrent_page() {
        return current_page;
    }

    public void setCurrent_page(int current_page) {
        this.current_page = current_page;
    }

    public int getLast_page() {
        return last_page;
    }

    public void setLast_page(int last_page) {
        this.last_page = last_page;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }
}
