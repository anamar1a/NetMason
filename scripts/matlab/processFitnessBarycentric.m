filename='C:\\MAStools\\workspace\\NetMason\\outputs\\multiEvaluations\\null_fitness.txt';
[h, data]=hdrload(filename);

%for epoch=1:10
    data=data(find((data(:,1)<950)),:);

    %if (length(data)>100)
    
    hold on;

    A = round(20*data(:, 3))/20';
    B = round(20*data(:, 4))/20';
    C = 1 - (A + B);
    D = 10+data(:, 6)';

    ternpcolor(A, B, D);
    shading interp;
    ternlabel('T1', 'T2', 'T3');
    colorbar;

    ternplot(0.12,0.13,0.77,'k.','LineWidth',20)

    hold off;

    filename = sprintf('C:\\MAStools\\workspace\\NetMason\\products\\RecursionPaper\\pictures\\MultiTeamFitnessEpoch%i.pdf',1);
    print('-dpdf', filename);
    
    %end
%end


boxplot(fit(:,5),fit(:,1),'whis',1.5,'symbol','w+');
xlabel('Epoch','FontSize',14);
ylabel('Expected profit','FontSize',14);

filename = 'C:\\MAStools\\workspace\\NetMason\\products\\RecursionPaper\\pictures\\SingleTeamEpochComparison.pdf';
print('-dpdf', filename);    