package com.jd.blockchain.ledger;

import com.jd.blockchain.contract.model.ContractDeployExeUtil;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 *
 * @Author zhaogw
 * @Date 2018/10/31 10:31
 */
public class IntegrationTest extends BaseTest{
    private int loopNum = 0;
    private boolean useLoop = false;

    /**
     * 在测试链中发布合约-执行合约-注册数据集；
     */
    private void deploy_exe_contract_registerDataAccount_on_test_gateway(){
        //then exe the contract;
        //由于合约发布之后需要后台进行共识处理，需要一定的时间消耗，先休息1秒钟之后再执行;
        try {
//            Thread.sleep(1000L);
            boolean deployResult = ContractDeployExeUtil.instance.deploy(host, port, ledger,ownerPubPath, ownerPrvPath, ownerPassword, chainCodePath,contractPub);
            System.out.println("deployResult="+deployResult);
            Thread.sleep(1000L);
            boolean exeResult = ContractDeployExeUtil.instance.exeContract(ledger,ownerPubPath, ownerPrvPath, ownerPassword,eventName,contractArgs);
            System.out.println("execute the contract,result= "+exeResult);
            boolean registerResult = registerDataAccount();
            System.out.println("generate dataAccount,result= "+registerResult);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在测试链上仅发布和执行合约;
     */
    private void deploy_exe_contract_on_test_gateway(){
        //then exe the contract;
        //由于合约发布之后需要后台进行共识处理，需要一定的时间消耗，先休息5秒钟之后再执行;
        try {
//            Thread.sleep(1000L);
            boolean deployResult = ContractDeployExeUtil.instance.deploy(host, port, ledger,ownerPubPath, ownerPrvPath, ownerPassword, chainCodePath,contractPub);
            System.out.println("deployResult="+deployResult);
            Thread.sleep(1000L);
            boolean exeResult = false;
            exeResult = ContractDeployExeUtil.instance.exeContract(ledger,ownerPubPath, ownerPrvPath, ownerPassword,eventName,contractArgs);
            System.out.println("execute the contract,result= "+exeResult);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册一个数据集，从合约中查验;
     * @return
     */
    private boolean registerDataAccount(){
        BlockchainKeyPair dataAccount = BlockchainKeyGenerator.getInstance().generate();
        //get the address,pubKey,privKey;
        System.out.println("dataAccount-addr-pubkey-privkey:"
                +dataAccount.getAddress()+","+dataAccount.getPubKey()+","+dataAccount.getPrivKey());

        // 定义交易,传输最简单的数字、字符串、提取合约中的地址;
        TransactionTemplate txTpl = bcsrv.newTransaction(ledgerHash);
        txTpl.dataAccounts().register(dataAccount.getIdentity());
        txTpl.dataAccount(dataAccount.getAddress()).
				set(param1,param1Val.getBytes(),-1).getOperation();
        System.out.println("registerDataAccount(),data address= "+dataAccount.getAddress());

        // 签名；
        PreparedTransaction ptx = txTpl.prepare();
        ptx.sign(ownerKey);

        // 提交并等待共识返回；
        TransactionResponse txResp = ptx.commit();

        // 验证结果；
        return txResp.isSuccess();
    }

    /**
     * 注册一个用户;
     * @return
     */
    private void registerUser() throws InterruptedException {
        //随机生成一个用户;
        BlockchainKeyPair userPair = BlockchainKeyGenerator.getInstance().generate();
        TransactionTemplate txTpl = bcsrv.newTransaction(ledgerHash);
        txTpl.users().register(userPair.getIdentity());
        System.out.println("userPair-addr-pubkey-privkey:"
                +userPair.getAddress()+","+userPair.getPubKey()+","+userPair.getPrivKey());

        // 签名；
        PreparedTransaction ptx = txTpl.prepare();
        ptx.sign(ownerKey);

        // 提交并等待共识返回；
        TransactionResponse txResp = ptx.commit();

        // 验证结果；
        Assert.assertTrue(txResp.isSuccess());
        //然后休息2秒后,验证是否有此用户;
        Thread.sleep(2000);
        //query
        UserInfo userInfo = bcsrv.getUser(ledgerHash,userPair.getAddress().toBase58());
        Assert.assertEquals(userPair.getAddress(),userInfo.getAddress());
    }

    /**
     * 注册用户;
     * @throws InterruptedException
     */
    @Test
    public void registerUser_more() throws InterruptedException {
        registerUser();
        if(useLoop){
            for (int i=0;i<loopNum;i++){
                registerUser();
            }
        }
    }

    /**
     * 数据集注册;
     */
    @Test
    public void register_dataAccount_more(){
        registerDataAccount();
        if(useLoop){
            for (int i=0;i<loopNum;i++){
                registerDataAccount();
            }
        }
    }

    /**
     * 发布执行合约;
     */
    @Test
    public void one_deploy_exe_contract_on_test_gateway(){
        this.deploy_exe_contract_on_test_gateway();
        if(useLoop){
            for (int i=0;i<loopNum;i++){
                this.deploy_exe_contract_on_test_gateway();
            }
        }
    }

    /**
     * 执行发布执行合约，然后注册数据集;
     */
    @Test
    public void one_deploy_exeContract_registerDataAccount_on_test_gateway(){
        this.deploy_exe_contract_registerDataAccount_on_test_gateway();
        if(useLoop){
            for (int i=0;i<loopNum;i++){
                this.deploy_exe_contract_registerDataAccount_on_test_gateway();
            }
        }
    }

    /**
     * 移植test相关测试用例;
     * 由于移植配置文件费时费力，只移植其逻辑，配置直接采用当前文件;
     */
    @Test
    public void batchInsertData_Test() {
        TransactionTemplate txTemp = bcsrv.newTransaction(ledgerHash);
        // --------------------------------------
        // 将商品信息写入到指定的账户中；
        // 对象将被序列化为 JSON 形式存储，并基于 JSON 结构建立查询索引；
//        String dataAccount = "GGhhreGeasdfasfUUfehf9932lkae99ds66jf==";
        BlockchainKeyPair dataAccount = BlockchainKeyGenerator.getInstance().generate();

        String key1 = "jd_key1";
        byte[] val1 = "www.jd.com".getBytes();

        String key2 = "jd_key2";
        byte[] val2 = "www.jd.com".getBytes();

        String key3 = "jd_key3";
        byte[] val3 = "www.jd.com".getBytes();

        // 定义交易,传输最简单的数字、字符串、提取合约中的地址;
        txTemp.dataAccounts().register(dataAccount.getIdentity());
        txTemp.dataAccount(dataAccount.getAddress()).set(key1, val1, -1);
        txTemp.dataAccount(dataAccount.getAddress()).set(key2, val2, -1);
        txTemp.dataAccount(dataAccount.getAddress()).set(key3, val3, -1);

        // TX 准备就绪；
        PreparedTransaction prepTx = txTemp.prepare();
        prepTx.sign(ownerKey);
        // 提交交易；
        TransactionResponse transactionResponse = prepTx.commit();

        assertTrue(transactionResponse.isSuccess());
    }

    @Test
    public void query_Test() {
        LedgerInfo ledgerInfo = bcsrv.getLedger(ledgerHash);
        long ledgerNumber = ledgerInfo.getLatestBlockHeight();
        System.out.println(ledgerNumber);
    }
}

