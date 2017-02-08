package org.neighbor.service.impl;

import org.neighbor.api.GeneralResponse;
import org.neighbor.api.dtos.CreateAccountRequest;
import org.neighbor.entity.NeighborAccount;
import org.neighbor.entity.NeighborOrg;
import org.neighbor.mappers.CreateAccountRequestToAccount;
import org.neighbor.repository.AccountRepository;
import org.neighbor.repository.OrgRepository;
import org.neighbor.service.AccountService;
import org.neighbor.service.UserService;
import org.neighbor.utils.ResponseGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CreateAccountRequestToAccount requestToAccountMapper;

    @Override
    public NeighborAccount createAccount(NeighborAccount account) {
        return accountRepository.save(account);
    }

    @Override
    public GeneralResponse createAccount(CreateAccountRequest createAccountRequest) {
        Optional<NeighborOrg> foundOrg = orgRepository.findByExtId(createAccountRequest.getOrgExtId());
        if (!foundOrg.isPresent())
            return ResponseGenerator.generateOrgNotExistError();
        NeighborOrg org = foundOrg.get();
        accountRepository.findDefaultByOrgIdAndAccountNumber(org.getId(), createAccountRequest.getAccountNumber());
        NeighborAccount account = requestToAccountMapper.createAccountRequestToAccount(createAccountRequest);
        String urn = createAccountRequest.getOrgExtId() + ":" + createAccountRequest.getAccountNumber();//todo
        account.setAccountUrn(urn);
        account.setOrg(org);
        account.setCreatedOn(new Date());
        accountRepository.save(account);
        return generateOkResponse(201);
    }

    @Override
    public void createDefaultAccountForOrgId(NeighborOrg org) {
        NeighborAccount account = createAccount(defaultForOrg(org));

//        userService.createDefaultUserForAccountId(account.getId());
    }

    @Override
    public Iterable<NeighborAccount> listByFilter(Object filter) {
        //// FIXME
        return accountRepository.findAll();
    }

    @Override
    public List<NeighborAccount> findByOrg(NeighborOrg byExtId) {
        return accountRepository.findByOrgId(byExtId.getId());
    }

    @Override
    public NeighborAccount findDefaultByOrgIdAndOwnerPhone(Long orgId, String ownerPhone) {
        return accountRepository.findDefaultByOrgIdAndOwnerPhone(orgId, ownerPhone);
    }

    @Override
    public void delete(NeighborAccount account) {
        accountRepository.delete(account);
    }

    @Override
    public NeighborAccount defaultForOrg(NeighborOrg org) {
        NeighborAccount account = new NeighborAccount();
        account.setOrg(org);
        account.setActive(true);
        account.setAccountNumber("0");
        account.setAddressBuilding("0");
        account.setAddressFlat("0");
        account.setAddressFloor("0");
        account.setAddressStreet("0");
        account.setCreatedOn(new Date());
        account.setOwnerPhone("0");
        account.setAccountUrn(org.getExtId() + ":" + account.getAccountNumber());
        return account;
    }

    @Override
    public Optional<NeighborAccount> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<NeighborAccount> findOrgAndAccountNumber(Long orgId, String accountNumber) {
        return accountRepository.findByOrgIdAndAccountNumber(orgId, accountNumber);
    }

    private GeneralResponse generateOkResponse(int code) {
        return new GeneralResponse(code, null);
    }

}