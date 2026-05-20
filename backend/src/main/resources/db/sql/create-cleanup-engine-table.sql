-- 删除旧表并创建新的表
DROP TABLE IF EXISTS clean_up_engine;

create table clean_up_engine
(
    id              INTEGER
        constraint clean_up_engine_pk
            primary key AUTOINCREMENT,
    name            TEXT,
    engine          TEXT,
    rule_type       TEXT,
    params          TEXT,
    description     TEXT,
    full_class_name TEXT
);

insert into clean_up_engine (id, name, engine, rule_type, params, description, full_class_name)
values  (1, '黑名单', 'BlackListEngine', 'FILTER', '[{"field":"keyword","label":"关键字","placeholder":null,"required":false,"type":"DYNAMIC_INPUT"}]', null, 'com.lemongo97.iptv.iptvmanager.engine.filter.BlackListEngine'),
        (2, 'FFProbe 检测', 'FFProbeCheckEngine', 'DELAY', '[{"field":"delayMinutes","label":"最高延迟时间","placeholder":null,"required":false,"type":"NUMBER"},{"field":"discardNoVideo","label":"丢弃无视频","placeholder":null,"required":false,"type":"INPUT"},{"field":"discardNoAudio","label":"丢弃无音频","placeholder":null,"required":false,"type":"INPUT"},{"field":"minVideoFrameWidth","label":"最小视频帧宽度","placeholder":null,"required":false,"type":"NUMBER"},{"field":"minVideoFrameHeight","label":"最小视频帧高度","placeholder":null,"required":false,"type":"NUMBER"}]', null, 'com.lemongo97.iptv.iptvmanager.engine.delay.FFProbeCheckEngine'),
        (3, '简繁转换', 'OpenCCEngine', 'NAME', '[{"field":"input","label":"输入语言","options":[{"value":"simple","label":"简体"},{"value":"traditional","label":"繁体"}],"placeholder":null,"required":false,"type":"SWITCH"},{"field":"output","label":"输出语言","options":null,"placeholder":null,"required":false,"type":"SWITCH"}]', null, 'com.lemongo97.iptv.iptvmanager.engine.name.OpenCCEngine'),
        (4, '大小写转换', 'CaseConversionEngine', 'NAME', '[{"field":"input","label":"输入","options":[{"value":"uppercase","label":"简体"},{"value":"lowercase","label":"繁体"}],"placeholder":null,"required":false,"type":"SWITCH"},{"field":"output","label":"输出","options":null,"placeholder":null,"required":false,"type":"SWITCH"}]', null, 'com.lemongo97.iptv.iptvmanager.engine.name.CaseConversionEngine'),
        (5, '正则替换', 'RegexReplaceEngine', 'NAME', '[{"field":"regex","label":"正则表达式","placeholder":null,"required":false,"type":"INPUT"},{"field":"groups","label":"分组替换设置","keyField":"groupId","keyPlaceholder":"分组ID","placeholder":null,"required":false,"type":"DYNAMIC_PAIR_INPUT","valueField":"text","valuePlaceholder":"替换值"}]', null, 'com.lemongo97.iptv.iptvmanager.engine.name.RegexReplaceEngine'),
        (6, '字符串替换', 'StringReplaceEngine', 'NAME', '[{"field":"target","label":"匹配值","placeholder":null,"required":false,"type":"INPUT"},{"field":"text","label":"替换文字","placeholder":null,"required":false,"type":"INPUT"}]', null, 'com.lemongo97.iptv.iptvmanager.engine.name.StringReplaceEngine'),
        (7, 'HTTP 检测', 'HttpCheckEngine', 'DELAY', '[{"field":"type","label":"检测方式","options":[{"value":"GET","label":"GET"},{"value":"HEAD","label":"HEAD"}],"placeholder":null,"required":false,"type":"SWITCH"},{"field":"delayMinutes","label":"最大延迟时间","placeholder":null,"required":false,"type":"NUMBER"}]', null, 'com.lemongo97.iptv.iptvmanager.engine.delay.HttpCheckEngine');