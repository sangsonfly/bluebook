#!/usr/bin/env python3
"""
蓝珊笔记 - 批量模拟用户操作脚本 (纯API版)
可重复运行，数据累积叠加
"""

import requests
import time
import json
import sys

# Windows 终端默认 GBK 不支持 emoji，强制使用 UTF-8
try:
    sys.stdout.reconfigure(encoding='utf-8')
except Exception:
    pass

# ============================================================
# 配置
# ============================================================
BASE = 'http://localhost:9090'  # 直连后端，不走 nginx（避免 /web/ 路径被 try_files 拦截）
RUN_ID = time.strftime('%H%M')

def green(s): return f'\033[92m{s}\033[0m'
def yellow(s): return f'\033[93m{s}\033[0m'
def red(s): return f'\033[91m{s}\033[0m'

stats = {
    'registered': 0,
    'notes_published': 0,
    'likes': 0,
    'collections': 0,
    'comments': 0,
}

# ============================================================
# 虚拟用户池
# ============================================================
USERS = [
    {'uname': f'sim_a_{RUN_ID}', 'nick': '图书馆常驻', 'persona': '学霸'},
    {'uname': f'sim_b_{RUN_ID}', 'nick': '食堂探店王', 'persona': '美食'},
    {'uname': f'sim_c_{RUN_ID}', 'nick': '操场小旋风', 'persona': '运动'},
    {'uname': f'sim_d_{RUN_ID}', 'nick': '摄影爱好者', 'persona': '摄影'},
    {'uname': f'sim_e_{RUN_ID}', 'nick': '宅寝码农', 'persona': '技术'},
    {'uname': f'sim_f_{RUN_ID}', 'nick': '社团达人', 'persona': '社交'},
    {'uname': f'sim_g_{RUN_ID}', 'nick': '考研战士', 'persona': '考研'},
    {'uname': f'sim_h_{RUN_ID}', 'nick': '文艺青年', 'persona': '文艺'},
    {'uname': f'sim_i_{RUN_ID}', 'nick': '二手市场王', 'persona': '交易'},
    {'uname': f'sim_j_{RUN_ID}', 'nick': '新生小萌', 'persona': '萌新'},
]
PASSWORD = 'test123456'

# ============================================================
# 笔记模板库
# ============================================================
NOTE_TEMPLATES = {
    '热门推荐': [
        {
            'title': '选课系统又崩了！分享一个抢课小技巧',
            'content': '今天早上9点准时守着选课系统，结果刚点进去就502了，试了五次才进去发现想选的影视鉴赏已经满了。不过我摸索出一个方法：用手机热点+浏览器无痕模式，比连校园网快很多，亲测抢到了三门课。另外建议提前把课程代码记在记事本里，进去直接搜索不要翻页，能省不少时间。大家有没有其他抢课秘诀分享一下？',
            'tags': '选课,抢课,技巧,校园',
        },
        {
            'title': '西门新开的瑞幸咖啡，9块9拿铁实测',
            'content': '西门出去左转200米新开了一家瑞幸，开业第一周全场9.9。今天去喝了生椰拿铁和厚乳拿铁，生椰的比厚乳的好喝，椰香很浓不会太甜。店里有座位也有WiFi，比图书馆咖啡馆安静，适合自习。营业时间7:00-21:00，早八前买一杯提神正好。不过高峰期排队大概10分钟，建议错开中午12点和下午3点这两个时间段。',
            'tags': '咖啡,瑞幸,探店,校园周边',
        },
        {
            'title': '校园网终于提速了！实测100M',
            'content': '之前吐槽校园网慢被信息化中心看到了，这周宿舍区网络全面升级完成。实测下载速度从之前的10M提到了100M，steam下游戏终于不用挂一晚了。教学楼和图书馆还没升完，据说下周搞定。不过上传还是慢只有20M，传大文件还是得用流量。另外IPv6也能用了，上Google Scholar不用挂梯子了。大家可以去信息化中心官网看升级公告。',
            'tags': '校园网,网络升级,实测,好消息',
        },
        {
            'title': '毕业季跳蚤市场本周六开市！摊位攻略',
            'content': '一年一度的毕业季跳蚤市场这周六在操场旁边开市，从早上9点到下午5点。大四学长学姐摆摊卖二手，每年都是淘宝贝的好机会。攻略：1. 早去（9点前到），好东西先被挑走；2. 带现金，很多摊位不用扫码；3. 电子产品和书籍是最划算的品类；4. 可以适当砍价但别太狠，学长学姐也不容易。去年我50块买了个九成新的机械键盘。',
            'tags': '跳蚤市场,毕业季,二手,攻略',
        },
        {
            'title': '这学期的十佳歌手大赛，有人一起去看吗',
            'content': '学生会主办的校园十佳歌手大赛下周五晚7点在大礼堂举行，免票入场。今年有30多人报名初选，决赛剩12人，听说有个大一学妹唱功特别厉害初选就惊艳全场。现场观众投票占比30%，可以去当大众评审。大礼堂空调比较冷建议带件外套。去年冠军唱的《起风了》现在还在B站有2万播放，我们学校还是有人才的。',
            'tags': '十佳歌手,比赛,校园活动,音乐',
        },
    ],
    '学习经验': [
        {
            'title': '大三老狗的高数复习攻略，亲测有效',
            'content': '高数真的是很多同学的噩梦，但其实掌握了方法就不难。我推荐先把课本上的例题全部做一遍，不要看答案，做完再对。然后去做近五年的期末考试卷子，会发现套路其实就那么几个。重点抓极限、导数和积分这三块，占分比最高。另外推荐B站宋浩老师的视频，讲得特别清楚。最后一个月每天保持2小时刷题量，及格肯定没问题。',
            'tags': '高数,复习,期末,学习方法',
        },
        {
            'title': '数据结构期末考试重点整理（附思维导图）',
            'content': '整理了一下数据结构的核心考点：1. 链表操作（增删改查+反转），考试必考；2. 二叉树遍历（前中后序+层次），要能手写递归和非递归版本；3. 图的最短路径（Dijkstra），理解算法思想；4. 排序算法对比，平均/最坏时间复杂度要记住；5. 哈希表冲突处理。总的来说这门课重理解而非背诵，建议多画图辅助理解。',
            'tags': '数据结构,期末考试,CS,思维导图',
        },
        {
            'title': '英语四六级阅读理解的正确做题顺序',
            'content': '好多同学都是从头做到尾，结果后面来不及。我摸索出来的最佳顺序是：先做Section C（仔细阅读），分值最高每篇10分，两篇做完了心态就稳了。然后做翻译，趁着脑子还清醒。再做Section B（长篇阅读匹配），这是拿分最快的部分。最后做Section A（选词填空），分值低、耗时长，实在来不及可以蒙。按这个顺序我阅读部分从150提到了195。',
            'tags': '四六级,英语,阅读理解,技巧',
        },
        {
            'title': '图书馆自习效率提升的5个小技巧',
            'content': '在图书馆泡了三年总结的经验：1. 提前一天列好第二天要完成的任务清单，越具体越好（不要写"复习数学"，要写"做完第三章课后题"）；2. 用番茄钟法，25分钟专注+5分钟休息，推荐用Forest App；3. 手机开勿扰模式放包里，不要放桌上；4. 选择一个固定的位置，形成条件反射；5. 带好降噪耳机或耳塞，图书馆总有窃窃私语的人。',
            'tags': '图书馆,自习,效率,学习技巧',
        },
        {
            'title': '大学四年绩点4.0的学霸时间管理法',
            'content': '期末了分享我的时间管理方法。核心工具就三个：Google Calendar + Notion + 一个实体手账本。日历用来安排固定时间块（上课、社团、运动），Notion用来跟踪作业DDL和项目进度，手账每天写三件最重要的事完成打勾。最重要的习惯是每天晚上花5分钟规划第二天，早上起来看一眼就知道要干嘛。另外，不要高估自己的意志力，把手机放在看不到的地方比靠自制力靠谱一百倍。',
            'tags': '时间管理,学霸,效率,期末',
        },
    ],
    '社团活动': [
        {
            'title': '羽毛球社本学期招新啦！零基础也欢迎',
            'content': '羽毛球社春季招新开始啦！我们每周三下午3-5点在体育馆固定活动，每月一次校内友谊赛，还会组织和外校的交流赛。不需要有基础，社里有经验丰富的老社员手把手教你。只要你喜欢运动，想交朋友，就快来加入吧！加入方式：校园蓝珊搜索"羽毛球社"申请加入，或者直接来体育馆找我们。这周三下午有新手体验活动，免费参加，提供球拍和球。',
            'tags': '羽毛球社,招新,运动,社团',
        },
        {
            'title': '周末志愿者活动：去流浪动物救助站',
            'content': '青协组织的周末志愿活动，这周六去市里的流浪动物救助站帮忙。主要是打扫猫舍狗舍、遛狗、给动物拍照发领养信息。救助站目前有30多只猫和20多只狗，都很亲人，但人手严重不足。我们学校已经有十几个同学报名了，再招5-8人。不需要有经验，有爱心就行。周六早上8点东门集合，包车去，大概下午3点回来。详情可以看青协的公众号。',
            'tags': '志愿者,流浪动物,周末活动,公益',
        },
        {
            'title': '摄影社春日外拍活动：一起去植物园',
            'content': '摄影社这周日组织去市植物园外拍，现在正是樱花和郁金香的花期。有相机的带相机，没相机的用手机也可以参加，社里会安排有经验的社员分组指导构图和调色。门票AA大概30元，公交直达半小时就到。上周踩点的时候发现植物园里有个小湖，下午3点左右光线打在水面上特别出片。集合时间周日上午8:30东门，想去的提前在社团群接龙报名。',
            'tags': '摄影社,外拍,春游,社团活动',
        },
        {
            'title': '辩论队纳新：嘴皮子利索的看过来',
            'content': '校辩论队春季补招4人，大一大二优先。我们队去年拿了省大学生辩论赛亚军，每周训练一次主要是即兴演讲和模拟辩论，不占用太多时间但很锻炼逻辑思维和表达能力。面试方式是现场抽一个话题准备3分钟然后做3分钟立论，不用紧张，主要看逻辑不是看口才。对辩论有兴趣或者想克服演讲恐惧症的同学都可以来试试，氛围很友好的。',
            'tags': '辩论队,纳新,演讲,思维训练',
        },
        {
            'title': '汉服社中秋雅集活动回顾，太美了',
            'content': '上周末汉服社在镜湖边办的中秋雅集真的太美了。三十几位同学穿着汉服在湖边赏月品茶，有古琴演奏、诗词接龙还有手作月饼体验。一个大四学姐穿了一套明制马面裙气质绝了，拍的照片在朋友圈刷屏。社长说下个月还有汉服出行日活动，会去古城墙那边取景，对汉服感兴趣的同学可以关注汉服社，不需要自己有汉服，社里可以借。',
            'tags': '汉服,雅集,传统文化,社团',
        },
    ],
    '校园活动': [
        {
            'title': '秋季运动会报名开始了！项目和时间安排',
            'content': '一年一度的秋季运动会定在11月3-4日，现在开始报名了。比赛项目：田赛有跳高跳远铅球，径赛有100/200/400/800/1500米和4×100接力。每个学院每个项目限报3人，想报名的同学去找辅导员或者体育委员。去年信息学院拿了总分第一，今年我们院要争取卫冕。不参赛的同学也欢迎来当啦啦队，现场气氛超好，还有学院摊位发零食和饮料。',
            'tags': '运动会,报名,比赛,校园活动',
        },
        {
            'title': '校园文化节来啦！美食摊位+音乐演出',
            'content': '第18届校园文化节下周三到周五在大草坪举行。这次有30多个社团摆摊，包括手工艺品展卖、书法体验、汉服试穿、机器人展示等等。还有各学院的美食摊位，去年计算机学院的烤冷面排了一个小时的队。晚上7点开始有乐队演出和街舞表演，去年还请了隔壁学校的说唱社团来助阵。入场免费，摊位消费需要校园卡。带上好心情来玩！',
            'tags': '文化节,校园活动,演出,美食',
        },
        {
            'title': '学术讲座预告：AI时代的大学生竞争力',
            'content': '学校邀请了某互联网大厂技术总监来开讲座，主题是"AI时代的大学生需要具备什么能力"。时间是下周二下午2:30-4:00在学术报告厅。讲者会分享行业最新动态、企业对人才的真实需求，还有互动问答环节。不只是计算机专业的同学适合听，产品、设计、运营方向也会涉及。建议大二大三的同学多去听这类讲座，对职业规划很有帮助。座位有限建议提前10分钟到场。',
            'tags': '讲座,AI,职业规划,学术活动',
        },
        {
            'title': '校园马拉松：5公里环校跑，奖牌超好看',
            'content': '第二届校园马拉松开始报名啦！路线是环校主干道跑两圈共5公里，不会太累新手也能完成。报名费20元包含一件参赛T恤和完赛奖牌，奖牌设计是学校钟楼的浮雕图案特别好看值得收藏。前100名完赛还有额外奖品。比赛时间是周六早上8点，7点半开始检录。不需要多快，走路完赛也有奖牌，主要是氛围好大家一起跑很开心。报名在体育馆一楼前台。',
            'tags': '马拉松,跑步,校园活动,运动',
        },
        {
            'title': '模拟联合国大会招募代表，锻炼国际视野',
            'content': '本学期模拟联合国大会开始招募各国代表啦！议题是"全球气候变化与可持续发展"，会期两天（周六日全天）。不需要模联经验，会前有一次培训教你议事规则和立场文件怎么写。参会能锻炼英语演讲、临场应变和谈判能力。我们学校代表队去年在全国模联大会上拿了最佳代表奖。报名方式：学校模联公众号填写报名表，截止这周五。名额有限先到先得！',
            'tags': '模联,模拟联合国,招募,国际视野',
        },
    ],
    '美食探店': [
        {
            'title': '二食堂二楼的麻辣香锅必须拥有姓名',
            'content': '开学第三周才发现二食堂二楼角落里新开了麻辣香锅！人均15-20块，荤素搭配自己选。推荐必点：牛肉片（很嫩）、腐竹（吸饱汤汁）、土豆片（炸过的那种）、鹌鹑蛋。微辣就已经很够味了，不能吃辣的同学慎选中辣以上。米饭免费续，两个人吃一顿才40出头，比外面划算太多。排队高峰期是11:50-12:15，建议11:30就去。',
            'tags': '食堂,麻辣香锅,美食推荐,性价比',
        },
        {
            'title': '测评学校周边4家奶茶店，第一名出乎意料',
            'content': '作为一个每周至少喝3杯奶茶的人，把学校周边奶茶店喝了个遍。测评维度：茶底质量、配料丰富度、性价比。第四名：西门某COCO，味道稳定但没特色。第三名：东门一点点，波霸好吃但整体偏甜。第二名：北门的茶百道，杨枝甘露是一绝。第一名：南门小巷里的"茶巷子"，手作珍珠+现泡茶底，14元一杯的茉莉奶绿秒杀所有连锁店，店主是个从台湾回来的大叔，用料很良心。',
            'tags': '奶茶,测评,校门口,美食',
        },
        {
            'title': '宿舍自制快手早餐合集，5分钟搞定',
            'content': '早八人必备！分享几个不用锅不用火的宿舍早餐：1. 隔夜燕麦杯：燕麦+牛奶/酸奶+水果，前一天晚上做好放窗外（冬天天然冰箱），早上直接吃；2. 三明治：吐司+火腿+芝士片+生菜，保鲜膜一包带走去教室；3. 红薯+鸡蛋：小功率蒸蛋器（宿舍神器）15分钟搞定；4. 冲泡类：藕粉/豆浆/黑芝麻糊，开水一冲完事。别再不吃早餐了！',
            'tags': '宿舍,早餐,懒人,美食DIY',
        },
        {
            'title': '东门外小吃街从头吃到尾，花费不到50',
            'content': '东门外那条小吃街我吃了三年，整理一份不踩雷清单：1. 巷口肉夹馍8元，老板西安人腊汁肉给得很足；2. 铁板豆腐6元一份，外焦里嫩撒上孜然绝了；3. 烤冷面+鸡蛋+烤肠10元，东北大哥做的酸甜酱是灵魂；4. 炸串自选素材新鲜，人均12；5. 最后来一碗冰粉5元解腻。从街头吃到街尾一个人不到50块撑到走不动。晚上6点后出摊最全。',
            'tags': '小吃街,美食,平价,探店',
        },
        {
            'title': '学校食堂隐藏菜单大公开',
            'content': '在学校吃了三年食堂发现了一些隐藏吃法：1. 一食堂麻辣烫窗口可以单点番茄汤底煮面，只要5块钱比牛肉面实惠；2. 三食堂早餐的煎饼果子加两个蛋多加薄脆阿姨不会多收钱；3. 二食堂小炒窗口其实可以自己带食材让师傅加工（收5-8块加工费），我们宿舍团建买了一大袋虾和花蛤去加工过；4. 食堂夜宵窗口9点后有当天剩下的包子馒头买一送一。知道的同学都在悄悄享受这些福利。',
            'tags': '食堂,隐藏菜单,省钱,美食攻略',
        },
    ],
    '二手市场': [
        {
            'title': '毕业季出闲置：机械键盘+显示器+台灯',
            'content': '大四离校出一些带不走的东西，都是自己用的保护得很好。1. IKBC C87机械键盘红轴，用了两年没毛病，150出（原价399）；2. AOC 24寸1080P显示器，HDMI接口，350出（原价899）；3. 小米台灯Pro，暖白可调，80出。打包500带走。东区宿舍自取。另外还有些专业书（计算机类）免费送，先到先得。',
            'tags': '毕业季,二手,键盘,显示器',
        },
        {
            'title': '出iPad Air4 64G + Apple Pencil二代',
            'content': '去年买的iPad Air4 64G深空灰，一直贴着钢化膜带着保护壳用，外观95新无磕碰。主要用来记笔记看网课，现在换Pro了所以出。电池健康度92%，配件齐全：原装充电器+数据线+Apple Pencil二代+磁吸类纸膜。单iPad 2800出，加笔打包3300。东区面交，当场验货。用了一年多最大的感受就是无纸化学习真的香，建议学弟学妹入手。',
            'tags': 'iPad,数码,二手,学习工具',
        },
        {
            'title': '出闲置吉他：YAMAHA F310 入门神器',
            'content': '去年买的YAMAHA F310民谣吉他，入门首选，音色在同价位没对手。原价799，现在450出。弹了不到半年就没什么时间练了，基本上和新的一样，弦是后来换的Elixir的（一套就要100多）。送调音器+变调夹+备用弦一套+入门教材一本+一个加厚海绵包。真心想学的同学来，可以教你基础和弦。西区宿舍，欢迎来试弹。',
            'tags': '吉他,乐器,闲置转让,音乐',
        },
        {
            'title': '出闲置相机：索尼A6000套机',
            'content': '索尼A6000微单+16-50套头，快门数大概8000多，外观有轻微使用痕迹但功能完好。原价3700入的，现2000出。对焦速度在这个价位无敌，拍风景和人像都够用，特别适合想入门摄影的同学。配件：原装电池2块+充电器+32G SD卡+相机包。另外还有个永诺50mm F1.8定焦头（E卡口），单出350，和机身打包2250。诚意买的可以小刀。',
            'tags': '相机,索尼,摄影器材,二手数码',
        },
        {
            'title': '免费送！大三搬宿舍清理出来的书和杂物',
            'content': '大三换校区，清了一批东西免费送给有缘人。主要是：1. 大学英语四级/六级词汇书和真题；2. 思修/马原/毛概/近代史课本；3. 几本闲书《三体》《活着》《追风筝的人》；4. 几个笔记本和文件夹。全部免费，先到先得。西区10号楼楼下的长椅上放着，大家自取就行。周五中午12点我会放出去。',
            'tags': '免费赠送,书籍,搬宿舍,福利',
        },
    ],
    '运动健身': [
        {
            'title': '操场夜跑三个月，从180减到150，分享我的经验',
            'content': '三月瘦了30斤，核心就四个字：管住嘴迈开腿。饮食方面：戒掉所有含糖饮料只喝水，晚饭主食减半，零食换成坚果和水果。运动方面：每周跑5次，每次5公里（大约25-30分钟），跑完做15分钟拉伸。推荐用Keep记录，看到数据变化特别有成就感。前两周最难熬，过了就习惯了。现在不跑反而浑身不舒服。操场晚上8点-10点人最多，气氛很好。',
            'tags': '跑步,减肥,操场,健身',
        },
        {
            'title': '体育馆羽毛球场地预约攻略',
            'content': '体育馆羽毛球场地工作日白天免费，晚上和周末5元/小时。预约需要提前一天早上8点在"校园体育"小程序抢，手慢无。建议组个4人小队，打双打最划算。球拍可以凭学生证免费借，但球要自带（推荐买亚狮龙的训练球，耐打）。体育馆的场地灯光很好，比户外的风小。如果有想学的新手，每周三下午羽毛球社有免费教学，老社员都很热情。',
            'tags': '羽毛球,体育馆,预约,运动',
        },
        {
            'title': '寝室健身指南：不用去健身房也能练',
            'content': '天冷不想出门？寝室也能练。准备两样东西：一张瑜伽垫（必备），一对可调节哑铃（20kg够用，某宝100左右）。训练计划：周一胸+三头（俯卧撑5组+哑铃卧推），周二背+二头（哑铃划船+弯举），周三休息，周四腿（深蹲+箭步蹲），周五肩（哑铃推举+侧平举），周末有氧去操场跑步。每天40分钟，三个月身材变化明显。关键是坚持不是强度。',
            'tags': '寝室健身,哑铃,自重训练,省钱',
        },
        {
            'title': '学校游泳馆开放时间和办卡攻略',
            'content': '学校游泳馆终于开放了！标准50米泳道，水温26-28度恒温，更衣室有热水淋浴。开放时间：周一至周五15:00-20:30，周末10:00-20:00。票价：学生单次15元，办学期卡150元无限次（超划算，去10次就回本）。记得带泳帽泳镜，没泳帽不让下水。周三四五下午人最少，周末下午小朋友多有点吵。不会游的可以报游泳课，体育部有零基础班200元8节课。',
            'tags': '游泳馆,运动,办卡,校园设施',
        },
        {
            'title': '篮球场改造完成了！新场地实测体验',
            'content': '操场旁边的四个篮球场全部翻新完毕，铺了新的塑胶地面比之前的硬地舒服太多了，摔了也不容易擦伤。篮筐也换了新的带弹簧的，手感很好。晚上灯光升级到LED了亮如白昼，打到10点熄灯没问题。这周下午去打了三次，每次都满场需要占位，建议下午4点前去。三分线也重新画了，标准FIBA距离。热爱篮球的同学这周末约起来！',
            'tags': '篮球场,校园设施,运动,翻新',
        },
    ],
    '校园风光': [
        {
            'title': '银杏大道又黄了！今年最佳观赏期预测',
            'content': '每年的11月中下旬是学校银杏大道最美的时候，根据今年的气温走势，大概11月15-25号是最佳观赏期。拍照建议：早上8-10点光线最柔，人也少；穿浅色衣服拍照好看（别穿黄的，会和背景融为一体）；可以带本书或者一杯咖啡当道具。摄影协会每年这时候都会组织外拍，想学的可以关注他们的公众号。另外，行政楼前面的几棵枫树也红了，别错过。',
            'tags': '银杏,秋天,校园风景,摄影',
        },
        {
            'title': '雨后的镜湖，美得像一幅水墨画',
            'content': '今天下了一上午的雨，下午放晴去镜湖边走了走，真的太美了。湖面上飘着一层薄雾，远处的图书馆倒映在湖水里，几只白鹭在浅水区踱步。湖边的柳树被雨水洗得翠绿，空气里是泥土和青草的味道。长椅上有个在拉小提琴的学长，配着雨后的景色特别有意境。这就是我为什么喜欢这个学校，总有一些瞬间让你觉得一切都值了。',
            'tags': '镜湖,雨后,校园风景,心情',
        },
        {
            'title': '图书馆顶楼日落机位分享',
            'content': '发现一个拍日落的宝藏位置：图书馆9楼自习区靠西的窗户。下午5点半左右太阳刚好从天际线往下沉，透过大玻璃窗洒进来光线是暖橙色的，拍剪影和逆光人像都绝了。靠窗有一排单人座位，带本书假装在看实际在等日落。周末人少更容易占到好位置。还有一个机位是主教学楼天台（需要找辅导员申请），视野更开阔可以看到整个校园和远处的山。',
            'tags': '图书馆,日落,摄影,机位',
        },
        {
            'title': '校园里的樱花开了，不比武大差',
            'content': '我们学校樱花其实也很能打！西区樱花大道两边种了上百棵染井吉野樱，现在正是满开期。风一吹花瓣飘落像下雪一样，地上铺了一层粉色地毯。这两天已经有不少校外的来拍照了，但比武大的人少多了不会人挤人。最佳观赏期大概还能持续一周，建议早上来光线好人也少。另外教学楼3号楼后面有几棵晚樱品种颜色更粉，知道的人不多是个隐藏打卡点。',
            'tags': '樱花,春天,校园风景,打卡',
        },
        {
            'title': '下雪后的校园，银装素裹的童话世界',
            'content': '昨晚下了一场大雪早上起来整个校园都白了。操场上的雪还没有被踩过像一块巨大的奶油蛋糕。图书馆的红色砖墙配上白色积雪特别好看，已经有几个摄影社的同学架着三脚架在拍了。镜湖结了一层薄冰，雪花落在冰面上有一种静谧的美。同学们在草坪上堆雪人打雪仗，南方的同学激动得不行说第一次见到这么大的雪。学校后勤已经在主干道撒了融雪剂，走路注意防滑。',
            'tags': '雪景,冬天,校园风光,摄影',
        },
    ],
    '其他': [
        {
            'title': '大一新生选课避坑指南',
            'content': '又到了选课季，作为踩过无数坑的大三老狗，给大家一些建议：1. 体育课千万别选太极拳，期末要打全套还要写论文；2. 通识选修课选"影视鉴赏"、"心理学与生活"，老师松给分高；3. "大学生职业规划"看着有用实际上很水，不如去听几场企业宣讲；4. 可以打听一下哪些老师不点名，但建议还是去上课，毕竟花了学费；5. 课表不要排太满，留出午休时间。',
            'tags': '选课,避坑,新生,大学',
        },
        {
            'title': '校园网最近好卡啊！有没有同感的',
            'content': '这周开始校园网延迟特别高，打游戏ping经常100+，看视频缓冲半天。去问了一下信息化中心说是在升级设备，预计下周恢复正常。不过听说升级后会提速到100M，期待一下。临时解决方案：用手机热点，联通的在我们学校信号比移动好。或者去图书馆三楼，那边的WiFi是独立的，速度还OK。有没有用电信的同学说说你们那边网速怎么样？',
            'tags': '校园网,吐槽,网络,求助',
        },
        {
            'title': '室友打呼噜怎么办？我的血泪经验总结',
            'content': '作为一个被室友呼噜声折磨了一年的人，我尝试了几乎所有办法：1. 3M耳塞（橙色那款），隔音效果最好但戴久了耳朵有点胀；2. 白噪音APP，混着呼噜声反而更容易入睡；3. 比室友先睡着，这个最管用但现在我变成了熬夜选手；4. 委婉提醒室友去看医生，他后来去查了发现是鼻炎引起的，治了之后好了很多。所以最好的办法是友好沟通+建议就医。',
            'tags': '室友,打呼噜,宿舍生活,经验',
        },
        {
            'title': '期末通宵复习地点推荐和避雷',
            'content': '期末季图书馆一座难求，分享几个备选：1. 教学楼2号楼5楼的自习室，知道的人少空位多，开到晚上11点；2. 食堂二楼晚上7点以后可以去，有灯有暖气还有夜宵；3. 宿舍楼的公共自习室，方便但容易被室友叫回去。避雷：教学楼主楼一楼大厅晚上冷得要命、行政楼旁边的连廊风大还吵。不管去哪通宵，带好充电宝、保暖外套和保温杯，身体要紧。',
            'tags': '期末,通宵,自习,备考',
        },
        {
            'title': '开学必带物品清单，新生看过来',
            'content': '开学季给新生列个行李清单：必带类：身份证、录取通知书、一寸两寸照片各10张（各种表格都要用）；生活类：床帘（一定要买遮光的，室友作息不同能救命）、插线板（至少3个插口+USB口）、台灯（充电款熄灯后也能用）；衣物类：南方同学记得带羽绒服，北方冬天比想象中冷；不用带：吹风机卷发棒（宿舍限功率，带了会跳闸），桶装水不用买学校有直饮水机。',
            'tags': '开学,新生,清单,宿舍',
        },
    ],
}

# ============================================================
# 评论模板
# ============================================================
COMMENTS = [
    '写得很好！收藏了',
    '感谢分享，很有帮助',
    '同感，我也遇到过类似的情况',
    '这个太实用了，给你点赞',
    '好帖顶一个',
    '请问楼主能详细说说吗？',
    '学到了，谢谢！',
    '这个食堂我也经常去，确实不错',
    '拍的太好看了吧！',
    '请问现在还有吗？',
    '已收藏，下次试试',
    '写得很详细，赞一个',
    '请问这个在哪里可以找到？',
    '深有同感！',
    '加一，我也觉得',
    '这个推荐太棒了',
    '请问可以转载吗？',
    '楼主多发点这样的笔记',
    '简直是我的互联网嘴替',
    '下次去看看',
]

# ============================================================
# API 函数
# ============================================================

def api_post(path, data, token=None):
    """统一的 API POST 请求"""
    headers = {}
    if token:
        headers['token'] = token
    try:
        resp = requests.post(f'{BASE}{path}', headers=headers, json=data, timeout=10)
        if not resp.text:
            return {}
        return resp.json()
    except requests.exceptions.ConnectionError:
        return {'code': '-1', 'msg': '无法连接服务器，请确认后端已启动'}
    except json.JSONDecodeError:
        return {'code': '-1', 'msg': f'服务器返回非JSON数据 (HTTP {resp.status_code})'}
    except Exception as e:
        return {'code': '-1', 'msg': str(e)}


def api_get(path, token=None):
    """统一的 API GET 请求"""
    headers = {}
    if token:
        headers['token'] = token
    try:
        resp = requests.get(f'{BASE}{path}', headers=headers, timeout=10)
        if not resp.text:
            return {}
        return resp.json()
    except requests.exceptions.ConnectionError:
        return {'code': '-1', 'msg': '无法连接服务器，请确认后端已启动'}
    except json.JSONDecodeError:
        return {'code': '-1', 'msg': f'服务器返回非JSON数据 (HTTP {resp.status_code})'}
    except Exception as e:
        return {'code': '-1', 'msg': str(e)}


def api_post_raw(path, token=None):
    """API POST 不带 body"""
    headers = {}
    if token:
        headers['token'] = token
    try:
        resp = requests.post(f'{BASE}{path}', headers=headers, timeout=10)
        return resp.status_code == 200
    except Exception:
        return False


def register_user(uname, nick):
    """注册用户"""
    print(f'  📝 注册 {uname} ({nick})...')
    body = api_post('/web/register', {
        'username': uname, 'password': PASSWORD, 'role': 'ROLE_USER'
    })
    if body.get('code') == '200':
        print(f'    ✅ {uname} 注册成功')
        stats['registered'] += 1
        return True
    elif '已存在' in str(body.get('msg', '')) or '已注册' in str(body.get('msg', '')):
        print(f'    ⚠️ {uname} 已存在')
        return True
    else:
        print(f'    ❌ {uname} 注册失败: {body.get("msg", "未知错误")}')
        return False


def login_user(uname, is_admin=False):
    """API 登录，返回 (uid, token, nickname)"""
    role = 'ROLE_ADMIN' if is_admin else 'ROLE_USER'
    body = api_post('/web/login', {
        'username': uname, 'password': PASSWORD, 'role': role
    })
    if body.get('code') == '200':
        data = body['data']
        return data['id'], data['token'], data.get('nickname', uname)
    else:
        print(f'    ❌ 登录失败: {body.get("msg", "")}')
        return 0, '', uname


def update_profile(token, uid, uname, nickname):
    """更新用户头像和昵称"""
    avatar = f'https://api.dicebear.com/7.x/initials/svg?seed={nickname}'
    body = api_post('/user', {
        'id': uid, 'username': uname, 'nickname': nickname, 'avatarUrl': avatar
    }, token=token)
    return avatar if body.get('code') == '200' else ''


def publish_note(token, uid, nickname, avatar, template):
    """发布笔记"""
    seed = int(time.time() * 1000) % 1000 + 1
    img_url = f'https://picsum.photos/seed/{seed}/400/300'
    body = api_post('/api/note', {
        'title': template['title'][:20],
        'content': template['content'][:1000],
        'imageUrl': img_url,
        'tags': template['tags'],
        'category': template.get('category', '学习经验'),
        'status': 1,
        'userId': uid,
        'authorName': nickname,
        'authorAvatar': avatar,
    }, token=token)
    if body.get('code') == '200':
        print(f'    📄 笔记: {template["title"][:30]}...')
        stats['notes_published'] += 1
        return True
    else:
        print(f'    ⚠️ 笔记创建失败: {body.get("msg", "未知")}')
        return False


def get_notes(token, page_size=50):
    """获取笔记列表（含 category、userId 等信息）"""
    body = api_get(f'/api/note/page?pageNum=1&pageSize={page_size}', token=token)
    records = body.get('data', {}).get('records', [])
    return records


def get_note_ids(token):
    """获取所有笔记 ID 列表"""
    records = get_notes(token)
    return [r['id'] for r in records if 'id' in r]


def like_note(token, uid, note_id):
    """点赞笔记"""
    return api_post_raw(f'/api/note/{note_id}/like?userId={uid}', token=token)


def collect_note(token, uid, note_id):
    """收藏笔记"""
    return api_post_raw(f'/api/note/{note_id}/collect?userId={uid}', token=token)


def comment_note(token, uid, nickname, avatar, note_id, content):
    """评论笔记"""
    body = api_post('/api/comment', {
        'noteId': note_id,
        'userId': uid,
        'content': content,
        'authorName': nickname,
        'authorAvatar': avatar,
    }, token=token)
    return body.get('code') == '200'


def follow_user(token, follower_id, followee_id):
    """关注用户"""
    return api_post_raw(
        f'/api/userFollow/follow?followerId={follower_id}&followeeId={followee_id}',
        token=token
    )


def send_message(token, sender_id, receiver_id, content):
    """发送私信"""
    return api_post_raw(
        f'/api/privateMessage/send?senderId={sender_id}&receiverId={receiver_id}&content={content}',
        token=token
    )


def join_club(token, uid, club_id):
    """加入社团"""
    body = api_post('/api/clubMember/join', {
        'clubId': club_id,
        'userId': uid,
    }, token=token)
    return body.get('code') == '200'


def create_activity(token, club_id, title, desc, location, start_time, end_time, max_parts, tags):
    """创建社团活动"""
    body = api_post('/api/activity/create', {
        'clubId': club_id,
        'title': title,
        'description': desc,
        'location': location,
        'startTime': start_time,
        'endTime': end_time,
        'maxParticipants': max_parts,
        'tags': tags,
    }, token=token)
    if body.get('code') == '200':
        return body.get('data', {}).get('id', 0) if isinstance(body.get('data'), dict) else 0
    return 0


def register_activity(token, uid, activity_id):
    """报名活动"""
    return api_post_raw(
        f'/api/activityRegistration/register?activityId={activity_id}&userId={uid}',
        token=token
    )


# ============================================================
# 主函数
# ============================================================
def main():
    print('=' * 60)
    print('  蓝珊笔记 — 批量用户模拟脚本 (纯API版)')
    print(f'  运行 ID: {RUN_ID}')
    print(f'  用户数: {len(USERS)}')
    print('=' * 60)

    # ---- Phase 1: 注册所有用户 ----
    print('\n📋 Phase 1: 注册用户')
    print('-' * 40)
    for u in USERS:
        register_user(u['uname'], u['nick'])

    # ---- Phase 2: 每个用户依次操作 ----
    print('\n🎮 Phase 2: 用户操作')
    print('-' * 40)

    # 人设 -> 分类 映射（Phase 2 和 Phase 3 共用）
    cat_map = {
        '学霸': '学习经验', '考研': '学习经验',
        '美食': '美食探店', '运动': '运动健身', '摄影': '校园风光',
        '社交': '社团活动', '文艺': '校园活动', '交易': '二手市场',
        '技术': '热门推荐', '萌新': '其他',
    }

    all_users = []  # [(uid, token, nickname, avatar), ...]

    for idx, u in enumerate(USERS):
        print(f'\n👤 [{idx+1}/{len(USERS)}] {u["nick"]} ({u["persona"]})')

        # 登录
        uid, token, nickname = login_user(u['uname'])
        if not uid:
            print(f'    ❌ 登录失败，跳过')
            continue

        # 生成头像（跳过 update_profile API，避免 500）
        avatar = f'https://api.dicebear.com/7.x/initials/svg?seed={u["nick"]}'
        all_users.append((uid, token, u['nick'], avatar))
        print(f'    🆔 ID={uid} 昵称={u["nick"]}')

        # 发布该人设对应分类下的全部笔记
        cat = cat_map.get(u['persona'], '其他')
        templates = NOTE_TEMPLATES.get(cat, [])
        if not templates:
            templates = NOTE_TEMPLATES.get('其他', [])
        for template in templates:
            t = template.copy()
            t['category'] = cat
            publish_note(token, uid, u['nick'], avatar, t)
            time.sleep(0.3)
        print(f'    📄 发布 {len(templates)} 篇笔记')

    # ---- Phase 3: 互动行为 ----
    print('\n💬 Phase 3: 互动行为 (90% 垂直 + 10% 探索)')
    print('-' * 40)

    import random as _random

    all_cats = list(NOTE_TEMPLATES.keys())

    for idx, (uid, token, nick, avatar) in enumerate(all_users):
        u = USERS[idx]
        persona = u['persona']
        main_cat = cat_map.get(persona, '其他')
        # 随机选 1 个不同分类作为 10% 探索
        explore_pool_cats = [c for c in all_cats if c != main_cat]
        explore_cat = _random.choice(explore_pool_cats)

        print(f'\n👤 [{idx+1}/{len(all_users)}] {nick} ({persona}) 主={main_cat} 探索={explore_cat}')

        notes = get_notes(token, page_size=60)
        # 分池：主兴趣池 + 探索池（允许互动自己笔记，保证演示数据量）
        main_pool = [n for n in notes
                     if n.get('category') == main_cat
                     and 'id' in n]
        explore_pool = [n for n in notes
                        if n.get('category') == explore_cat
                        and 'id' in n]

        if not main_pool:
            print(f'    ⚠️ 主池无笔记，跳过')
            continue

        # 随机选 3-4 篇点赞+收藏，1-2 篇评论
        n_total = _random.randint(3, 4)
        n_comment = _random.randint(1, 2)

        # 每篇独立: 90% 主池, 10% 探索池
        selected = []
        for _ in range(n_total):
            if _random.random() < 0.9 or not explore_pool:
                pool = main_pool
            else:
                pool = explore_pool
            available = [n for n in pool if n not in selected]
            if not available:
                available = [n for n in main_pool if n not in selected]
            if available:
                selected.append(_random.choice(available))

        n_comment = min(n_comment, len(selected))
        to_comment = selected[:n_comment]

        for note in selected:
            nid = note['id']
            ntitle = note.get('title', '')[:25]
            if like_note(token, uid, nid):
                stats['likes'] += 1
                print(f'    ❤️ 点赞: {ntitle}')
            if collect_note(token, uid, nid):
                stats['collections'] += 1
                print(f'    ⭐ 收藏: {ntitle}')
            time.sleep(0.2)

        for note in to_comment:
            nid = note['id']
            ntitle = note.get('title', '')[:25]
            comment_text = _random.choice(COMMENTS)
            if comment_note(token, uid, nick, avatar, nid, comment_text):
                stats['comments'] += 1
                print(f'    💬 评论: {ntitle} -> "{comment_text}"')
            time.sleep(0.2)

    # ---- 最终统计 ----
    print('\n' + '=' * 60)
    print('  📊 执行统计')
    print('=' * 60)
    print(f'  注册用户:  {stats["registered"]}')
    print(f'  发布笔记:  {stats["notes_published"]}')
    print(f'  点赞笔记:  {stats["likes"]}')
    print(f'  收藏笔记:  {stats["collections"]}')
    print(f'  评论笔记:  {stats["comments"]}')
    total_ops = stats['registered'] + stats['notes_published'] + stats['likes'] + stats['collections'] + stats['comments']
    print(f'  {"─" * 30}')
    print(f'  总操作数:  {total_ops}')
    print(f'\n  ✅ 脚本执行完毕！可重新运行以累积更多数据。')


if __name__ == '__main__':
    main()
