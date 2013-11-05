if exists (select 1 from sysobjects where id = object_id('sp_select_group') and type = 'P')
begin
   drop proc sp_select_group
   print 'Procedure sp_select_group supprimee'
end
go

create proc sp_select_group as begin select 1 end
go

if exists (select 1 from sysobjects where id = object_id('sp_select_group') and type = 'P')
   print 'Procedure sp_select_group cree'
go
