if exists (select 1 from sysobjects where id = object_id('sp_select_group_person') and type = 'P')
begin
   drop proc sp_select_group_person
   print 'Procedure sp_select_group_person supprimee'
end
go

create proc sp_select_group_person as begin select 1 end
go

if exists (select 1 from sysobjects where id = object_id('sp_select_group_person') and type = 'P')
   print 'Procedure sp_select_group_person cree'
go
