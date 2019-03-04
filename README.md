# ShadowsocksDemo

# 纯Java版Shadowsocks源码，根据[官方kotlin版](https://github.com/shadowsocksrr/shadowsocksr-android)翻译成java版

# 用法示例：在如下initData(VpnActivity)方法中配置自己的设备信息

```Java
 private void initData() {
        try {
            ProfileManager.clear();

            Profile profile1 = new Profile();
            profile1.setHost("103.115.44.");
            profile1.setIpv6(true);
            profile1.setMethod("aes-256-cfb");
            profile1.setPassword("123");
            profile1.setRemoteDns("8.8.8.8");
            profile1.setRemotePort(2444);
            profile1.setRoute("all");
            profile1.setUdpdns(false);
            profile1.setName("线路1");
            ProfileManager.createProfile(profile1);

            Core.switchProfile(profile1.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        updateSelectedRoute();
    }
```
