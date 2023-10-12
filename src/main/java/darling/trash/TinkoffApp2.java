package darling.trash;

import ru.tinkoff.piapi.contract.v1.InstrumentStatus;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InvestApi;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class TinkoffApp2 {
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
        var token = "t._qVKudqLJNPnr9lSaPG93sbVMPyqAf4D985DzrNawqqB764SJwmGTRtLlxsWU3QPVKRWrfhE0CmZjyjJe8olEQ";
        var api = InvestApi.create(token);
        List<Share> shares = null;
        try {
            shares = api.getInstrumentsService().getShares(InstrumentStatus.INSTRUMENT_STATUS_BASE).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
//        PrintWriter out = new PrintWriter("c:\\temp\\1.txt");
        Set<String> ex = new HashSet<>();
        for (Share share : shares) {
            if (RealExchange.REAL_EXCHANGE_MOEX.equals(share.getRealExchange())) {
                ex.add(share.getExchange());
            }

        }
//        out.close();
        System.out.println(ex);

    }

}
