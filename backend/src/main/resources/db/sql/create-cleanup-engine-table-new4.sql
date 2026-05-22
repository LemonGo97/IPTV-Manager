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


INSERT INTO clean_up_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name") VALUES (1, '黑名单', 'BlackListEngine', 'FILTER', '[{"field":"keyword","label":"关键字","placeholder":null,"required":false,"type":"DYNAMIC_INPUT"}]', NULL, 'com.lemongo97.iptv.iptvmanager.engine.filter.BlackListEngine');
INSERT INTO clean_up_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name") VALUES (2, 'FFProbe 检测', 'FFProbeCheckEngine', 'DELAY', '[{"field":"delayMillisecond","label":"最高延迟时间(ms)","placeholder":null,"required":false,"type":"NUMBER"},{"field":"discardNoVideo","label":"丢弃无视频","placeholder":null,"required":false,"type":"SWITCH"},{"field":"discardNoAudio","label":"丢弃无音频","placeholder":null,"required":false,"type":"SWITCH"},{"field":"minVideoFrameWidth","label":"最小视频帧宽度","placeholder":null,"required":false,"type":"NUMBER"},{"field":"minVideoFrameHeight","label":"最小视频帧高度","placeholder":null,"required":false,"type":"NUMBER"}]', NULL, 'com.lemongo97.iptv.iptvmanager.engine.delay.FFProbeCheckEngine');
INSERT INTO clean_up_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name") VALUES (3, '简繁转换', 'OpenCCEngine', 'NAME', '[{"field":"input","label":"输入语言","options":[{"value":"simple","label":"简体"},{"value":"traditional","label":"繁体"}],"placeholder":null,"required":false,"type":"SELECT"},{"field":"output","label":"输出语言","options":[{"value":"simple","label":"简体"},{"value":"traditional","label":"繁体"}],"placeholder":null,"required":false,"type":"SELECT"}]', NULL, 'com.lemongo97.iptv.iptvmanager.engine.name.OpenCCEngine');
INSERT INTO clean_up_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name") VALUES (4, '大小写转换', 'CaseConversionEngine', 'NAME', '[{"field":"input","label":"输入","options":[{"value":"uppercase","label":"大写"},{"value":"lowercase","label":"小写"}],"placeholder":null,"required":false,"type":"SELECT"},{"field":"output","label":"输出","options":[{"value":"uppercase","label":"大写"},{"value":"lowercase","label":"小写"}],"placeholder":null,"required":false,"type":"SELECT"}]', NULL, 'com.lemongo97.iptv.iptvmanager.engine.name.CaseConversionEngine');
INSERT INTO clean_up_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name") VALUES (5, '正则替换', 'RegexReplaceEngine', 'NAME', '[{"field":"regex","label":"正则表达式","placeholder":null,"required":false,"type":"INPUT"},{"field":"groups","label":"分组替换设置","keyField":"groupId","keyPlaceholder":"分组ID","placeholder":null,"required":false,"type":"DYNAMIC_PAIR_INPUT","valueField":"text","valuePlaceholder":"替换值"}]', NULL, 'com.lemongo97.iptv.iptvmanager.engine.name.RegexReplaceEngine');
INSERT INTO clean_up_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name") VALUES (6, '字符串替换', 'StringReplaceEngine', 'NAME', '[{"field":"target","label":"匹配值","placeholder":null,"required":false,"type":"INPUT"},{"field":"text","label":"替换文字","placeholder":null,"required":false,"type":"INPUT"}]', NULL, 'com.lemongo97.iptv.iptvmanager.engine.name.StringReplaceEngine');
INSERT INTO clean_up_engine ("id", "name", "engine", "rule_type", "params", "description", "full_class_name") VALUES (7, 'HTTP 检测', 'HttpCheckEngine', 'DELAY', '[{"field":"type","label":"检测方式","options":[{"value":"GET","label":"GET"},{"value":"HEAD","label":"HEAD"}],"placeholder":null,"required":false,"type":"SELECT"},{"field":"delayMillisecond","label":"最大延迟时间(ms)","placeholder":null,"required":false,"type":"NUMBER"}]', NULL, 'com.lemongo97.iptv.iptvmanager.engine.delay.HttpCheckEngine');