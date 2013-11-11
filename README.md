# websocket-hol

## これは何？

[JJUG CCC 2013 Fall](http://www.java-users.jp/?page_id=695) での、以下 寺田さんのハンズオンを WildFly に移植したものです。

* [R5-1 Java EEハンズオン](http://www.java-users.jp/?page_id=709#r5-1)
* [アプリケーション概要](http://yoshio3.com/2013/10/23/java-ee-7-hol-on-jjug-ccc/)

## 必要なもの

* JDK 7
* [Maven 3.x](http://maven.apache.org/download.cgi)
* [WildFly 8.0.0.Beta1](http://www.wildfly.org/download/)

WildFLy のインストールディレクトリを $WILDFLY_HOME と表記します。

## アプリケーションのビルド

~~~
$ git clone https://github.com/emag/websocket-hol
$ cd websocket-hol
$ mvn clean package
~~~

websocket-hol を $PROJECT と表記します。
$PROJECT/target に、websocket-hol.war が作成されます。

## スタンドアロン環境での実行

WildFly をスタンドアロンで起動します。
なお、messaging サブシステムを利用するので、standalone-full.xml を指定します。

~~~
$ cd $WILDFLY_HOME/bin
$ ./standalone.sh -c standalone-full.xml
~~~

アプリケーションで利用するトピックを登録します。

~~~
$ cd $WILDFLY_HOME/bin
$ ./jboss-cli.sh -c --file=$PROJECT/inforegtopic-add-standalone.cli
~~~

アプリケーションをデプロイします。

~~~
$ cd $WILDFLY_HOME/bin
$ ./jboss-cli.sh -c --command='deploy /vagrant/websocket-hol.war'
~~~

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

// TODO HornetQ クラスタリング設定
// TODO デプロイ
