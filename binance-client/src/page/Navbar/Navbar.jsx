import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
    SheetTrigger,
} from "@/components/ui/sheet"
import { DragHandleHorizontalIcon, MagnifyingGlassIcon } from "@radix-ui/react-icons"

import React from 'react'
import { Sidebar } from "../Sidebar/Sidebar"

export const Navbar = () => {
    return (
        <div className='px-2 py-3 border-b z-50 bg-background bg-opacity-0 sticky top-0 left-0 right-0
    flex justify-between items-center'>
            <div className='flex items-center gap-3'>
                <Sheet>
                    <SheetTrigger>
                        <Button variant="ghost" size="icon" className="rounded-full h-11 w-11">
                            <DragHandleHorizontalIcon className="h-7 w-7" />
                        </Button>
                    </SheetTrigger>
                    <SheetContent side="left" className='w-72 border-r-0 flex flex-col justify-center'>
                        <SheetHeader >
                            <SheetTitle>
                                <div className='text-3xl flex justify-center items-center gap-1'>
                                    <Avatar>
                                        <AvatarImage src='https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Binance_Logo.svg/768px-Binance_Logo.svg.png' />
                                    </Avatar>
                                    <div>
                                        <span className="font-bold text-yellow-500">BINANCE</span>
                                    </div>
                                </div>
                            </SheetTitle>
                            <Sidebar />
                        </SheetHeader>
                    </SheetContent>
                </Sheet>
                <p className="text-sm lg:text-base cursor-pointer font-bold text-yellow-500">BINANCE</p>
                <div className="p-0 ml-9">
                    <Button variant='outline' className='flex items-center gap-3'>
                        <MagnifyingGlassIcon />
                        <span>Search</span>
                    </Button>
                </div>
            </div>
            <div>
                <Avatar>
                    <AvatarFallback>TQ</AvatarFallback>
                </Avatar>
            </div>
        </div>
    )
}
