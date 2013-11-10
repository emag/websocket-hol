# websocket-hol

## これは何？

JJUG CCC 2013 Fall での、寺田さんのハンズオンを WildFly に移植したものです。

## 必要なもの

* JDK 7
* [Maven 3.x](http://maven.apache.org/download.cgi)
* [WildFly 8.0.0.Beta1](http://www.wildfly.org/download/)

## スタンドアロン環境での実行

## ドメイン環境での実行

ドメインモードでそれぞれ 3 ノードの wildfly を起動します。
1つのマシンで 3 ノードを起動し、それぞれのベースとなるディレクトリを machine1, machine2, machine3 とします。
それぞれの役割は以下です。

* machine1 : ドメインコントローラ
* machine2 : ホストコントローラ
* machine3 : ホストコントローラ

~~~
$ cd $WILDFLY_HOME
$ cp -r domain machine1
$ cp -r domain machine2
$ cp -r domain machine3
~~~

machine2/configuration/host-slave.xml を編集します。

[変更前]
~~~
[...]
// 3行目付近
<host xmlns="urn:jboss:domain:2.0">
[...]
~~~

[変更後]
~~~
[...]
// 3行目付近
<host name="host2" xmlns="urn:jboss:domain:2.0">
[...]
~~~

machine3/configuration/host-slave.xml を編集します。

[変更前]
~~~
[...]
// 3行目付近
<host xmlns="urn:jboss:domain:2.0">
[...]
// 85行目付近
    <servers>
        <server name="server-one" group="main-server-group"/>
        <server name="server-two" group="other-server-group">
            <!-- server-two avoids port conflicts by incrementing the ports in
                 the default socket-group declared in the server-group -->
            <socket-bindings port-offset="150"/>
        </server>
    </servers>
~~~

[変更後]
~~~
[...]
// 3行目付近
<host name="host3" xmlns="urn:jboss:domain:2.0"> // (1) name 属性の変更
[...]
// 85行目付近
    <servers>
        <server name="server-three" group="other-server-group"> // (2) 既存のサーバを削除し、port-offset が 1000 の server-three を追加
            <socket-bindings port-offset="1000"/>
        </server>
    </servers>
~~~

ドメインコントローラを起動します。
`<ip-addr>` には、ネットワークインタフェースの IP アドレスを指定してください。

~~~
$ ./domain.sh --host-config=host-master.xml -Djboss.domain.base.dir=../machine1 -Djboss.bind.address.management=<ip-addr>
~~~

メッセージングのクラスタリング設定(ユーザとパスワード)を行います。
`<ip-addr>` はドメインコントローラの -Djboss.bind.address.management で指定した値と同一です。

~~~
$ ./jboss-cli.sh -c --controller=<ip-addr>:9990 --command='/profile=full-ha/subsystem=messaging/hornetq-server=default:write-attribute(name=cluster-user,value="ClusterUser")'
$ ./jboss-cli.sh -c --controller=<ip-addr>:9990 --command='/profile=full-ha/subsystem=messaging/hornetq-server=default:write-attribute(name=cluster-password,value="ClusterPassword")'
~~~


1つ目のホストコントローラを起動します。
`<ip-addr>` はドメインコントローラの -Djboss.bind.address.management で指定した値と同一です。

~~~
$ ./domain.sh --host-config=host-slave.xml -Djboss.domain.base.dir=../machine2 -Djboss.domain.master.address=<ip-addr> -Djboss.management.native.port=19999
~~~

2つ目のホストコントローラを起動します。
`<ip-addr>` はドメインコントローラの -Djboss.bind.address.management で指定した値と同一です。

~~~
$ ./domain.sh --host-config=host-slave.xml -Djboss.domain.base.dir=../machine3 -Djboss.domain.master.address=<ip-addr> -Djboss.management.native.port=29999
~~~