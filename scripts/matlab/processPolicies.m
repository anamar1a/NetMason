filename='C:\\MAStools\\workspace\\NetMason\\outputs\\temp\\sampleRun.txt';
[h, data]=hdrload(filename);

data=data(find(data(:,1)<1000),:);

% Process income rate as function of time

colors=[1 0 0; 0 0 1; 0 1 0];

hold on;

for numComp=0:1
    
indicies=find(data(:,2)==numComp);    

fitx=log(1+data(indicies,1));
fity=data(indicies,6)./data(indicies,10);

r=ksr(fitx,fity,1,100);

mins=10000000+zeros(length(r.x),1);
maxs=zeros(length(r.x),1)-10000000;

for c=1:20
    th=rand(length(fitx),1);
    indicies=find(th(:)>0.95);
    tfitx=fitx(indicies);
    tfity=fity(indicies);
    r=ksr(tfitx,tfity,0.5,100);
    mins=min(mins,r.f');
    maxs=max(maxs,r.f');
end

r=ksr(fitx,fity,0.5,100);

ciplot(mins,maxs,r.x,colors(numComp+1,:));
plot(r.x,r.f,'LineWidth',2,'Color',0.5*colors(numComp+1,:));

end

hold off;

xlabel('Time','FontSize',14);
ylabel('income','FontSize',14);
legend('95% CI for duopoly','Average for duopoly','95% CI for triopoly','Average for triopoly');

filename = 'D:\\MAStools\\workspace\\Telco\\products\\graphics\\BESTRevenuePerProfileAvgDegree4.pdf';
print('-dpdf', filename);

% Plot a single run

filename='C:\\MAStools\\workspace\\NetMason\\outputs\\temp\\incomeRate.txt';
[h, data]=hdrload(filename);

wind=1;

for id=1:7000

    close all;

    vec=data(find(data(:,19)==id),:);

    if (length(vec)>200)

        if ((vec(1,15)==2) && (vec(1,17)==2) && (vec(1,14) < 0.3) && (vec(1,16) ==1) && (vec(1,18) ==1))

            for company=0:1

                rel=vec(find(vec(:,2)==company),:);

                subplot(2,1,company+1)

                hold all;
                jbfill(rel(:,1)',0*rel(:,1)',smooth(rel(:,3),wind)','b','k',1);
                jbfill(rel(:,1)',smooth(rel(:,3),wind)',smooth(rel(:,3)+rel(:,4),wind)','r','k',1);
                jbfill(rel(:,1)',smooth(rel(:,3)+rel(:,4),wind)',1+0*rel(:,1)','g','k',1);
                hold off;

                legend('T1','T2','T3');

                if (company==0)
                    %lab=sprintf('Levels [%i,%i] Horizons[%i,%i] Delta=%+1.1f Beta=%+1.1f',rel(1,15),rel(1,17),rel(1,16),rel(1,18),rel(1,14),rel(1,13))
                    title('Player A','FontSize',14);
                    ylabel('P_A(T\bullet)','FontSize',12);
                
                else
                    title('Player B','FontSize',14);
                    ylabel('P_B(T\bullet)','FontSize',12);
                     
                end

                axis([2 100 0 1]);
                alpha(1);

            end

            xlabel('Time','FontSize',14);


            filename = sprintf('C:\\MAStools\\workspace\\NetMason\\products\\RecursionPaper\\pictures\\policyTrajectories_%i_%i_%i_%i_%+1.1f_%+1.1f_%i.pdf',rel(1,15),rel(1,17),rel(1,16),rel(1,18),rel(1,14),rel(1,13),id);
            print('-dpdf', filename);

        end

    end
end

% Plot a single run in barycentric coordinates

filename='C:\\MAStools\\workspace\\NetMason\\outputs\\temp\\incomeRate.txt';
[h, data]=hdrload(filename);

wind=1;

for id=1:400

    close all;

    vec=data(find(data(:,19)==id),:);

    if (length(vec)>200)

        if ((vec(1,15)>1) && (vec(1,17) >1) && (vec(1,14) < 0.3) && (vec(1,16) < 3) && (vec(1,18) < 3))

            for company=0:1

                rel=vec(find(vec(:,2)==company),:);

                subplot(1,2,company+1)

                cline(rel(:,3),rel(:,4),[],rel(:,1))
               
                xlabel('T1','FontSize',14);
                ylabel('T2','FontSize',14);

                if (company==0)
                    title('Player A','FontSize',14);
                else
                    title('Player B','FontSize',14);
                    colormap;
                end

                axis tight;

            end




            filename = sprintf('C:\\MAStools\\workspace\\NetMason\\products\\RecursionPaper\\pictures\\baryCentricTrajectories_%i_%i_%i_%i_%+1.1f_%+1.1f_%i.pdf',rel(1,15),rel(1,17),rel(1,16),rel(1,18),rel(1,14),rel(1,13),id);
            print('-dpdf', filename);

        end

    end
end




