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
    ],
    '美食': [
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
    ],
    '校园风景': [
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
    ],
    '二手市场': [
        {
            'title': '毕业季出闲置：机械键盘+显示器+台灯',
            'content': '大四离校出一些带不走的东西，都是自己用的保护得很好。1. IKBC C87机械键盘红轴，用了两年没毛病，150出（原价399）；2. AOC 24寸1080P显示器，HDMI接口，350出（原价899）；3. 小米台灯Pro，暖白可调，80出。打包500带走。东区宿舍自取，需要的私聊发图。另外还有些专业书（计算机类）免费送，先到先得。',
            'tags': '毕业季,二手,键盘,显示器',
        },
        {
            'title': '有没有人要拼单买收纳架？宿舍必备',
            'content': '最近发现一个超好用的宿舍收纳神器——床边挂篮，直接挂在床栏上，能放手机、充电器、水杯、纸巾，再也不用下床拿东西了。某宝3个装19.9包邮，有没有一起拼的？凑5个人以上还能再便宜。另外推荐那种免打孔墙上置物架，贴在书桌上方放书和小摆件，承重不错，我已经用了半年没掉过。需要的评论区扣1，我拉群。',
            'tags': '拼单,宿舍,收纳,好物推荐',
        },
        {
            'title': '大四清宿舍：考研书籍白菜价出',
            'content': '考研结束了，出一批考研资料，基本都是正版，部分有笔记但不影响阅读。1. 张宇高数18讲+线代9讲+概率9讲，三本打包40出；2. 李永乐复习全书（数二），15出；3. 英语黄皮书历年真题（05-25），20出；4. 肖秀荣1000题+精讲精练+八套卷四套卷，打包35出。买多可刀，东区12号楼自取。希望这些书能帮到下一届考研人！',
            'tags': '考研,书籍,二手教材,毕业季',
        },
        {
            'title': '出iPad Air4 64G + Apple Pencil二代',
            'content': '去年618京东买的iPad Air4 64G深空灰，一直贴着钢化膜带着保护壳用，外观95新无磕碰。主要用来记笔记看网课，现在换Pro了所以出。电池健康度92%，配件齐全：原装充电器+数据线+Apple Pencil二代+磁吸类纸膜。单iPad 2800出，加笔打包3300。东区面交，当场验货。用了一年多最大的感受就是无纸化学习真的香，建议学弟学妹入手。',
            'tags': 'iPad,数码,二手,学习工具',
        },
        {
            'title': '转让健身卡：还剩8个月，价格美丽',
            'content': '学校西门外那个"力美健身"的年卡，刚办两个月，因为要去外地实习了用不上，转让给有需要的同学。原价1280/年，还剩10个月（到明年3月），现在600转。器械比较全，跑步机、史密斯架、哑铃区都有，人也比市区的健身房少很多，晚上去基本不用排队。转卡费我来出，有兴趣的私聊看卡面。',
            'tags': '健身卡,转让,运动,二手',
        },
        {
            'title': '出闲置吉他：YAMAHA F310 入门神器',
            'content': '去年买的YAMAHA F310民谣吉他，入门首选，音色在同价位没对手。原价799，现在450出。弹了不到半年就没什么时间练了，基本上和新的一样，弦是后来换的Elixir的（一套就要100多）。送调音器+变调夹+备用弦一套+入门教材一本+一个加厚海绵包。真心想学的同学来，可以教你基础和弦。西区宿舍，欢迎来试弹。',
            'tags': '吉他,乐器,闲置转让,音乐',
        },
        {
            'title': '毕业大甩卖：台灯/风扇/衣架/收纳箱等生活用品',
            'content': '毕业离校，宿舍里的东西能卖就卖，带不走的都出了。1. 美的LED台灯（三档调光）25出；2. 小桌扇USB充电款 15出；3. 衣架30个铁质的5块钱打包；4. 透明收纳箱中号3个一起20；5. 床帘（遮光款）25出；6. 懒人手机支架 5块。买满50送懒人支架或衣架，全部一起80打包带走。东区15号楼，最好这两三天来拿。',
            'tags': '毕业季,生活用品,甩卖,便宜',
        },
        {
            'title': '出闲置相机：索尼A6000套机',
            'content': '索尼A6000微单+16-50套头，快门数大概8000多，外观有轻微使用痕迹但功能完好。原价3700入的，现2000出。对焦速度在这个价位无敌，拍风景和人像都够用，特别适合想入门摄影的同学。配件：原装电池2块+充电器+32G SD卡+相机包。另外还有个永诺50mm F1.8定焦头（E卡口），单出350，和机身打包2250。诚意买的可以小刀。',
            'tags': '相机,索尼,摄影器材,二手数码',
        },
        {
            'title': '求二手自行车一辆，上下课用',
            'content': '有没有学长学姐出自行车的？不要太好也不要太破，能骑就行。学校太大了从宿舍到教学楼走要20分钟实在受不了。预算150以内，最好是女式车或者小轮车（腿短跨不上大车架）。折叠车也可以考虑，宿舍好放。要求：刹车要好使、轮胎不漏气、座椅高度可调。有出的直接评论区发图或者私聊，这两天就可以交易。谢谢！',
            'tags': '求购,自行车,出行,校园',
        },
        {
            'title': '出全新未拆封电动牙刷替换头8个装',
            'content': '618凑单买的飞利浦电动牙刷替换头HX6013标准型8个装，结果发现我用的型号不匹配（我是HX6系列，这个是HX9系列适用）。全新未拆封，原价159，现在80出。适用的型号：HX93xx/HX91xx/DiamondClean系列。不确定自己能不能用的可以查一下刷头兼容表。校内交易，也可快递（邮费自理）。',
            'tags': '电动牙刷,配件,闲置全新,数码',
        },
        {
            'title': '出闲置好物：桌面小电器合集',
            'content': '整理了一批桌面小电器，都是自己买多了/升级换下来的，功能都正常。1. 米家加湿器（4L大容量）45出，冬天开空调必备；2. 小熊电煮锅1.5L，偶尔煮泡面煮粥，35出；3. 飞科吹风机1600W，20出；4. 宿舍小台灯暖光款，10块。打包全部带走90，也可以单独买。全部清洁干净了，放心使用。东区3号楼。',
            'tags': '小家电,桌面,闲置,实惠',
        },
        {
            'title': '免费送！大三搬宿舍清理出来的书和杂物',
            'content': '大三换校区，清了一批东西免费送给有缘人。主要是：1. 大学英语四级/六级词汇书和真题（用过但很新）；2. 思修/马原/毛概/近代史课本（全新基本没翻过）；3. 几本闲书《三体》《活着》《追风筝的人》；4. 几个笔记本和文件夹。全部免费，先到先得，一样都不留。西区10号楼楼下的长椅上放着，大家自取就行。周五中午12点我会放出去。',
            'tags': '免费赠送,书籍,搬宿舍,福利',
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
            'content': '青协组织的周末志愿活动，这周六去市里的流浪动物救助站帮忙。主要是打扫猫舍狗舍、遛狗、给动物拍照发领养信息。救助站目前有30多只猫和20多只狗，都很亲人，但人手严重不足。我们学校已经有十几个同学报名了，再招5-8人。不需要有经验，有爱心就行。周六早上8点东门集合，包车去，大概下午3点回来。想去的私我拉你进群。',
            'tags': '志愿者,流浪动物,周末活动,公益',
        },
    ],
    '寝室生活': [
        {
            'title': '室友打呼噜怎么办？我的血泪经验总结',
            'content': '作为一个被室友呼噜声折磨了一年的人，我尝试了几乎所有办法：1. 3M耳塞（橙色那款），隔音效果最好但戴久了耳朵有点胀；2. 白噪音APP，混着呼噜声反而更容易入睡；3. 比室友先睡着，这个最管用但现在我变成了熬夜选手；4. 委婉提醒室友去看医生，他后来去查了发现是鼻炎引起的，治了之后好了很多。所以最好的办法是友好沟通+建议就医。',
            'tags': '室友,打呼噜,宿舍生活,经验',
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


def get_note_ids(token):
    """获取所有笔记 ID 列表"""
    body = api_get('/api/note/page?pageNum=1&pageSize=50', token=token)
    records = body.get('data', {}).get('records', [])
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
        cat_map = {
            '学霸': '学习经验', '技术': '学习经验', '考研': '学习经验',
            '美食': '美食', '运动': '运动健身', '摄影': '校园风景',
            '社交': '社团活动', '文艺': '其他', '交易': '二手市场',
            '萌新': '其他',
        }
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

    # ---- 最终统计 ----
    print('\n' + '=' * 60)
    print('  📊 执行统计')
    print('=' * 60)
    print(f'  注册用户:  {stats["registered"]}')
    print(f'  发布笔记:  {stats["notes_published"]}')
    print(f'  {"─" * 30}')
    print(f'  总操作数:  {stats["registered"] + stats["notes_published"]}')
    print(f'\n  ✅ 脚本执行完毕！可重新运行以累积更多数据。')


if __name__ == '__main__':
    main()
