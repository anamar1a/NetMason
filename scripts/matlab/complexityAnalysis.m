filename='C:\\MAStools\\workspace\\NetMason\\outputs\\temp\\incomeRate.txt';
[h, data]=hdrload(filename);

wind=1;

hold on;

for id=1:1000
    vec=data(find(data(:,19)==id),:);
    if (length(vec)>200)
        if ((vec(1,15)==2) && (vec(1,17)==2) && (vec(1,14) < 0.3) && (vec(1,16) ==1) && (vec(1,18) ==1))      
            scatter3(rel(:,3),rel(:,4),rel(:,13));
        end 
    end
end

hold off;

 

vec=data(find((data(:,19)<10000)  & (data(:,15)==2) & (data(:,15)==2) & (data(:,17)==2) & (data(:,14) > 0.5) & (data(:,16) ==1) & (data(:,18) ==1) &(data(:,2)==0)),:);
S=5*ones(length(vec),1);
C=vector2colors(vec(:,13));
scatter(vec(:,3),vec(:,4),S,C,'filled')
view(-60,60);


counter=1;
estimates=zeros(100,3);

for id=1:7000
    vec=data(find(data(:,19)==id),:);
    if (length(vec)>20)
        if ((vec(1,15)==2) && (vec(1,17)==2) && (vec(1,14) < 1) && (vec(1,16) ==1) && (vec(1,18) ==1))      
           [H,sigma]=hurst(vec(:,3));
          
           estimates(counter,1)=H;
           estimates(counter,2)=sigma;
           estimates(counter,3)=vec(1,13);
           estimates(counter,4)=vec(1,14);
           counter = counter + 1;
           
           id
        end 
    end
end