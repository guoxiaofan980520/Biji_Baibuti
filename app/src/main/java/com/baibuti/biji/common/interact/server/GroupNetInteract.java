package com.baibuti.biji.common.interact.server;

import com.baibuti.biji.common.auth.AuthManager;
import com.baibuti.biji.common.retrofit.RetrofitFactory;
import com.baibuti.biji.common.interact.contract.IGroupInteract;
import com.baibuti.biji.model.dto.GroupDTO;
import com.baibuti.biji.model.po.Group;
import com.baibuti.biji.model.vo.MessageVO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GroupNetInteract implements IGroupInteract {

    @Override
    public Observable<MessageVO<List<Group>>> queryAllGroups() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getAllGroups()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<List<Group>>(false, responseDTO.getMessage());
                List<Group> fromGroups = new ArrayList<>();
                Collections.addAll(fromGroups, GroupDTO.toGroups(responseDTO.getData()));
                return new MessageVO<>(fromGroups);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Group>> queryGroupById(int id) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getGroupById(id)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Group>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toGroup());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Group>> queryGroupByName(String name) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getGroupByName(name)
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Group>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toGroup());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<MessageVO<Group>> queryDefaultGroup() {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .getDefaultGroup()
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Group>(false, responseDTO.getMessage());
                return new MessageVO<>(responseDTO.getData().toGroup());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | DUPLICATED
     */
    @Override
    public Observable<MessageVO<Boolean>> insertGroup(Group group) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .insertGroup(group.getName(), group.getColor())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                group.setId(responseDTO.getData().getId());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED | DUPLICATED | DEFAULT
     */
    @Override
    public Observable<MessageVO<Boolean>> updateGroup(Group group) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateGroup(group.getId(), group.getName(), group.getOrder(), group.getColor())
            .map(responseDTO -> {
                if (responseDTO.getCode() != 200)
                    return new MessageVO<Boolean>(false, responseDTO.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * SUCCESS | FAILED
     */
    @Override
    public Observable<MessageVO<Boolean>> updateGroupsOrder(Group[] groups) {
        String[] id_order = new String[groups.length];
        for (int i = 0; i < groups.length; i++)
            id_order[i] = groups[i].getId() + "_" + groups[i].getOrder();

        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .updateGroupsOrder(id_order)
            .map(response -> {
                if (response.getCode() != 200)
                    return new MessageVO<Boolean>(false, response.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @return SUCCESS | FAILED  | DEFAULT
     */
    @Override
    public Observable<MessageVO<Boolean>> deleteGroup(int id, boolean isToDefault) {
        return RetrofitFactory.getInstance()
            .createRequest(AuthManager.getInstance().getAuthorizationHead())
            .deleteGroup(id, isToDefault)
            .map(response -> {
                if (response.getCode() != 200)
                    return new MessageVO<Boolean>(false, response.getMessage());
                return new MessageVO<>(true);
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}
