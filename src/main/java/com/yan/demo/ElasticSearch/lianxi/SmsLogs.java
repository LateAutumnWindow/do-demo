package com.yan.demo.ElasticSearch.lianxi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsLogs {

    // 唯一ID
    @JsonIgnore
    private String id;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date createDate;
    // 发送时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date sendDate;
    // 发送的长号码
    private String longCode;
    // 下发手机号
    private String mobile;
    // 发送公司名称
    private String corpName;
    // 下发短信内容
    private String smsContent;
    // 短信下发状态 0 OK   1 error
    private Integer state;
    // 运营商编号 1 移动 2 联通 3 电信
    private Integer operatorId;
    // 省份
    private String province;
    // 下发服务器IP
    private String ipAddr;
    // 短信状态报告返回时长秒
    private Integer replyTotal;
    // 费用
    private Integer fee;


    public static String doc = "近几年bai来，我们看到了我们伟大的祖国的科技事du业的迅猛发展，这让我为zhi我是个dao中国人而感到无比的自豪。记得很久以前，手机的用途几乎只有一个，那就是打电话，可是前几年，手机有了很大的改变，不仅外观漂亮多了，而且用途也多了，可以用手机拍照、开会、上网、发短信息等等一系列的事情，这让我们的生活更为方便，也让我更加领会到了科技的力量，不过，我只是个初出茅庐的学生，对“科技”二字的内容还知之有限，我无法用一些很深奥的理论来阐述科技的玄奇，也无法对各位走上工作岗位的长辈们承诺我所能实现的科技蓝图。但我愿意用一个学生的角度来畅想科技与未来。\n" +
            "从基因工程“让人活到一千岁”的梦想，到纳米技术“包你穿衣不用洗”的诺言；从人工智能“送你一只可爱机器狗”的温馨，到转基因技术“让老鼠长出人耳朵”的奇观。不断有新的科技在诞生，每一个新科技的发现都会让人们欣喜若狂，因为，这些新科技正在逐步地改善我们的生活，让我们更加了解自己。就近期而言，中国首先完成了非典病毒全基因组测序，非典现在是全球公认的危害性最大的疾病，可是为什么别的国家不能首先完成，而我们国家就偏偏完成了呢？很简单，这说明了我们国家不比别人落后，不比别人差，回头看看我们祖国的过去，从曾经一个刚刚起步的改革开放的国家到现在的拥有领先的科技水平的大国，我们的祖国经历了多少的风风雨雨，多少的困难与坎坷，但是我们的祖国还是挺过来了，因为我们的祖国坚信——科技不仅改变命运，还可改变未来。\n" +
            "对于我们这一代人，对社会的普遍感觉是竞争意识强了，学习劲头足了。科普知识是我们关注的焦点，爱因斯坦、霍金、比尔·盖茨是我们心目中的明星，计算机科学、现代物理和化学动态更是无时不牵动着我们。我们已经明白科技的重要性，也知道了科技的普遍性。\n" +
            "虽然科技创造新生活的前景引人遐思，令人神往。但是归根结底是要靠我们共同的努力实现的。作为祖国未来建设的中坚，我们这一代年轻人肩上的担子的确不轻，新的机遇总是伴着风险与挑战，但是，我们不会轻易地说放弃，我们用我们的青春向前辈们发誓：决不辜负前辈们对我们的希望。\n" +
            "回望文明的历程，是科技之光扫荡了人类历史上蒙昧的黑暗，是科学之火点燃了人类心灵中的熊熊的希望；科技支撑了文明，科技创造着未来，而未来在我们手中。让我们成为知识的探索者，让我们在未知的道路上漫游，";
}
