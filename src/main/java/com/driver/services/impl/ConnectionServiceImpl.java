package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception {
        User user = userRepository2.findById(userId).get();
        if (user.getMaskedIp() != null) {
            throw new Exception("user already connected");
        }
        if (countryName.equalsIgnoreCase(user.getCountry().getCountryName().name())) {
            return user;

        } else if (user.getServiceProviderList() == null) {
            throw new Exception("unable to connect");

        }
        ServiceProvider serviceProvider1 = null;
        Country country1 = null;

        List<ServiceProvider> providerlist = user.getServiceProviderList();
        for (ServiceProvider x : providerlist) {
            List<Country> countylist = x.getCountryList();
            for (Country y : countylist) {
                if (countryName.equalsIgnoreCase(y.getCountryName().toString())) {
                    serviceProvider1 = x;
                    country1 = y;
                }
            }

        }
        if(serviceProvider1!=null){
            Connection connection = new Connection();
            connection.setServiceProvider(serviceProvider1);
            connection.setUser(user);
            String countycode = country1.getCode();
            String providerid = String.valueOf(serviceProvider1.getId());
            String maskedip = countycode+"."+providerid+"."+userId;
            user.setMaskedIp(maskedip);
            user.setConnected(true);
            user.getConnectionList().add(connection);
            serviceProvider1.getConnectionList().add(connection);
            userRepository2.save(user);
            serviceProviderRepository2.save(serviceProvider1);
            return user;


        }
        else {
            throw new Exception("unable to connect");
        }
    }


    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if(user.getConnected()== false)
            throw new Exception("Already disconnected");

        user.setMaskedIp(null);
        user.setConnected(false);
        userRepository2.save(user);
        return user;

    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {

        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();

        if(receiver.getMaskedIp()!=null){

            String receiverMaskedIp = receiver.getMaskedIp();

            String code= receiverMaskedIp.substring(0,3);

            if(code.equals(sender.getCountry().getCode()))
                return sender;
            else {
                String countryName = "";

                if (code.equals(CountryName.CHI.toCode()))
                    countryName = CountryName.CHI.toString();

                if (code.equals(CountryName.JPN.toCode()))
                    countryName = CountryName.JPN.toString();

                if (code.equals(CountryName.IND.toCode()))
                    countryName = CountryName.IND.toString();

                if (code.equals(CountryName.USA.toCode()))
                    countryName = CountryName.USA.toString();

                if (code.equals(CountryName.AUS.toCode()))
                    countryName = CountryName.AUS.toString();

                try{
                    User updatedSender = connect(senderId,countryName);
                    return updatedSender;

                }catch (Exception e){
                    throw new Exception("Cannot establish communication");
                }
            }
        }else{
            if(receiver.getCountry().equals(sender.getCountry())){
                return sender;

            }else{
                String countryName = receiver.getCountry().getCountryName().toString();
                try{
                    User updatedSender = connect(senderId,countryName);
                    return updatedSender;

                }catch (Exception e){
                    throw new Exception("Cannot establish communication");
                }
            }
        }
    }


}

